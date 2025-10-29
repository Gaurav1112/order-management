package com.peerisland.orderManagement.repository;

import com.peerisland.orderManagement.model.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    // kept simple — usually not required directly, but available for ad-hoc queries
}
