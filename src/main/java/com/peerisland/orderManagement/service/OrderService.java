package com.peerisland.orderManagement.service;

import com.peerisland.orderManagement.dto.CreateOrderRequest;
import com.peerisland.orderManagement.dto.OrderResponse;
import com.peerisland.orderManagement.dto.OrderStatusHistoryDto;
import com.peerisland.orderManagement.exception.NotFoundException;
import com.peerisland.orderManagement.model.OrderEntity;
import com.peerisland.orderManagement.model.OrderItemEntity;
import com.peerisland.orderManagement.model.OrderStatus;
import com.peerisland.orderManagement.model.OrderStatusHistoryEntity;
import com.peerisland.orderManagement.repository.OrderRepository;
import com.peerisland.orderManagement.repository.OrderStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core order business logic: create (idempotent), read, list, cancel, update, and batch bump.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepo;
    private final OrderStatusHistoryRepository oshRepo;

    private static final int BATCH_SIZE = 200;

    public OrderService(OrderRepository orderRepo, OrderStatusHistoryRepository oshRepo) {
        this.orderRepo = orderRepo;
        this.oshRepo = oshRepo;
    }

    /**
     * Idempotent create: if clientRequestId provided and an order exists with that id,
     * return that order instead of creating a duplicate.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        if (req.getClientRequestId() != null && !req.getClientRequestId().isBlank()) {
            var existing = orderRepo.findByClientRequestId(req.getClientRequestId());
            if (existing.isPresent()) {
                log.info("Idempotent create: returning existing order for clientRequestId={}", req.getClientRequestId());
                return OrderResponse.fromEntity(existing.get());
            }
        }

        OrderEntity order = new OrderEntity();
        order.setClientRequestId(req.getClientRequestId());
        order.setCustomerName(req.getCustomerName());

        req.getItems().forEach(dtoItem -> {
            OrderItemEntity item = new OrderItemEntity();
            item.setSku(dtoItem.getSku());
            item.setName(dtoItem.getName());
            item.setQuantity(dtoItem.getQuantity());
            item.setPrice(dtoItem.getPrice());
            order.addItem(item);
        });

        order.recalcTotal();
        OrderEntity saved = orderRepo.save(order);
        log.info("Created order id={} clientRequestId={}", saved.getId(), saved.getClientRequestId());
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return orderRepo.findById(id)
                        .map(OrderResponse::fromEntity)
                        .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAll(Integer page, Integer size, OrderStatus status) {
        int p = page == null ? 0 : page;
        int s = size == null ? 50 : size;
        if (status == null) {
            return orderRepo.findAll(PageRequest.of(p, s))
                            .stream().map(OrderResponse::fromEntity).collect(Collectors.toList());
        } else {
            return orderRepo.findByStatus(status, PageRequest.of(p, s))
                            .stream().map(OrderResponse::fromEntity).collect(Collectors.toList());
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        OrderEntity order = orderRepo.findById(id)
                                     .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order can be cancelled only when PENDING.");
        }

        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.touchUpdatedAt();

        try {
            OrderEntity saved = orderRepo.save(order);
            log.info("Cancelled order id={}", saved.getId());
            // Record status history
            oshRepo.save(new OrderStatusHistoryEntity(saved, previous.name(), OrderStatus.CANCELLED.name(), "SYSTEM"));
            return OrderResponse.fromEntity(saved);
        } catch (ObjectOptimisticLockingFailureException ole) {
            log.warn("Optimistic locking failure while cancelling order id={}", id, ole);
            throw ole;
        }
    }

    /**
     * Update order status and record status history.
     */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        OrderEntity order = orderRepo.findById(id)
                                     .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));

        OrderStatus prev = order.getStatus();

        if (!StateMachine.canTransition(prev, newStatus)) {
            throw new IllegalStateException("Invalid status transition: " + prev + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        order.touchUpdatedAt();
        try {
            OrderEntity saved = orderRepo.save(order);
            log.info("Order id={} status updated to {}", id, newStatus);

            // Record history
            oshRepo.save(new OrderStatusHistoryEntity(saved, prev == null ? null : prev.name(), newStatus.name(), "SYSTEM"));

            return OrderResponse.fromEntity(saved);
        } catch (ObjectOptimisticLockingFailureException ole) {
            log.warn("Optimistic locking failure while updating status for order id={}", id, ole);
            throw ole;
        }
    }

    /**
     * Bump up to BATCH_SIZE oldest PENDING orders to PROCESSING.
     * Returns number of orders updated.
     */
    @Transactional
    public int bumpPendingToProcessingBatch() {
        var page = PageRequest.of(0, BATCH_SIZE);
        List<Long> ids = orderRepo.findPendingOrderIds(OrderStatus.PENDING, page);
        if (ids.isEmpty()) {
            return 0;
        }

        List<OrderEntity> orders = orderRepo.findAllById(ids);
        int updated = 0;
        for (OrderEntity o : orders) {
            if (o.getStatus() == OrderStatus.PENDING &&
                StateMachine.canTransition(o.getStatus(), OrderStatus.PROCESSING)) {
                o.setStatus(OrderStatus.PROCESSING);
                o.touchUpdatedAt();
                oshRepo.save(new OrderStatusHistoryEntity(o, OrderStatus.PENDING.name(), OrderStatus.PROCESSING.name(), "SCHEDULER"));
                updated++;
            }
        }
        orderRepo.saveAll(orders);
        log.info("Bumped {} orders from PENDING to PROCESSING", updated);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDto> getStatusHistory(Long orderId) {
        return oshRepo.findByOrderIdOrderByChangedAtAsc(orderId)
                      .stream()
                      .map(h -> new OrderStatusHistoryDto(
                          h.getId(),
                          h.getPreviousStatus(),
                          h.getNewStatus(),
                          h.getChangedBy(),
                          h.getChangedAt()))
                      .collect(Collectors.toList());
    }
}
