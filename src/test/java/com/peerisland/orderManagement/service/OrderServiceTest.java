package com.peerisland.orderManagement.service;

import com.peerisland.orderManagement.dto.CreateOrderRequest;
import com.peerisland.orderManagement.dto.OrderResponse;
import com.peerisland.orderManagement.dto.OrderStatusHistoryDto;
import com.peerisland.orderManagement.exception.NotFoundException;
import com.peerisland.orderManagement.model.*;
import com.peerisland.orderManagement.repository.OrderRepository;
import com.peerisland.orderManagement.repository.OrderStatusHistoryRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private OrderStatusHistoryRepository oshRepo;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity sampleOrder;

    @BeforeEach
    void setup() {
        sampleOrder = new OrderEntity();
        sampleOrder.setId(1L);
        sampleOrder.setClientRequestId("REQ-1");
        sampleOrder.setCustomerName("John Doe");
        sampleOrder.setStatus(OrderStatus.PENDING);
        sampleOrder.setCreatedAt(OffsetDateTime.now());
        sampleOrder.setUpdatedAt(OffsetDateTime.now());
    }

    @Test
    void testCreateOrder_NewOrder_Success() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setClientRequestId("REQ-123");
        req.setCustomerName("Alice");
        req.setItems(List.of(new CreateOrderRequest.Item("SKU1", "Laptop", 1, 999.99d)));

        when(orderRepo.findByClientRequestId("REQ-123")).thenReturn(Optional.empty());
        when(orderRepo.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        OrderResponse resp = orderService.createOrder(req);

        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getCustomerName()).isEqualTo("Alice");
        verify(orderRepo, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void testCreateOrder_Idempotent_ReturnsExisting() {
        when(orderRepo.findByClientRequestId("REQ-1")).thenReturn(Optional.of(sampleOrder));

        CreateOrderRequest req = new CreateOrderRequest();
        req.setClientRequestId("REQ-1");
        req.setCustomerName("John Doe");

        OrderResponse resp = orderService.createOrder(req);

        assertThat(resp.getId()).isEqualTo(sampleOrder.getId());
        verify(orderRepo, never()).save(any());
    }

    @Test
    void testGetById_Found() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));

        OrderResponse resp = orderService.getById(1L);

        assertThat(resp.getId()).isEqualTo(1L);
        verify(orderRepo, times(1)).findById(1L);
    }

    @Test
    void testGetById_NotFound_ThrowsException() {
        when(orderRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getById(99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void testCancelOrder_WhenPending_Success() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepo.save(any(OrderEntity.class))).thenReturn(sampleOrder);
        when(oshRepo.save(any(OrderStatusHistoryEntity.class))).thenReturn(new OrderStatusHistoryEntity());

        OrderResponse resp = orderService.cancelOrder(1L);

        assertThat(resp.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepo).save(any(OrderEntity.class));
        verify(oshRepo).save(any(OrderStatusHistoryEntity.class));
    }

    @Test
    void testCancelOrder_WhenNotPending_ThrowsIllegalState() {
        sampleOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("can be cancelled only when PENDING");
    }

    @Test
    void testCancelOrder_OptimisticLockingFailure() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepo.save(any(OrderEntity.class)))
            .thenThrow(new ObjectOptimisticLockingFailureException(OrderEntity.class, 1L));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void testUpdateStatus_ValidTransition_Success() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepo.save(any(OrderEntity.class))).thenReturn(sampleOrder);

        OrderResponse resp = orderService.updateStatus(1L, OrderStatus.PROCESSING);

        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(oshRepo).save(any(OrderStatusHistoryEntity.class));
    }

    @Test
    void testUpdateStatus_InvalidTransition_Throws() {
        sampleOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepo.findById(1L)).thenReturn(Optional.of(sampleOrder));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PROCESSING))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Invalid status transition");
    }

    @Test
    void testGetStatusHistory_ReturnsDtos() {
        OrderStatusHistoryEntity e1 = new OrderStatusHistoryEntity(sampleOrder, "PENDING", "PROCESSING", "SYSTEM");
        e1.setId(1L);
        e1.setChangedAt(OffsetDateTime.now());
        when(oshRepo.findByOrderIdOrderByChangedAtAsc(1L)).thenReturn(List.of(e1));

        List<OrderStatusHistoryDto> list = orderService.getStatusHistory(1L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).previousStatus()).isEqualTo("PENDING");
    }
}
