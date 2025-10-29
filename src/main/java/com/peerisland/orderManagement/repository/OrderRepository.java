package com.peerisland.orderManagement.repository;

import com.peerisland.orderManagement.model.OrderEntity;
import com.peerisland.orderManagement.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Standard find by status with pagination (used for list APIs)
    List<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    // Light-weight query: only select IDs of oldest PENDING orders — used by scheduler to fetch batches.
    @Query("select o.id from OrderEntity o where o.status = :status order by o.createdAt asc")
    List<Long> findPendingOrderIds(@Param("status") OrderStatus status, Pageable pageable);

    // Idempotency lookup
    Optional<OrderEntity> findByClientRequestId(String clientRequestId);
}
