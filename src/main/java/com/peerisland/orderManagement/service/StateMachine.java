package com.peerisland.orderManagement.service;

import com.peerisland.orderManagement.model.OrderStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

/**
 * Centralized rule-set for allowed status transitions.
 */
public final class StateMachine {
    private static final EnumMap<OrderStatus, Set<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED));
        ALLOWED.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private StateMachine() {}

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }
}
