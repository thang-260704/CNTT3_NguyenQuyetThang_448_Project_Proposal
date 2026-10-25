package com.rikkei.quickshop.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Vòng đời đơn hàng: PENDING -> PROCESSING -> SHIPPING -> COMPLETED, hoặc CANCELLED (khi chưa giao).
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPING,
    COMPLETED,
    CANCELLED;

    public Set<OrderStatus> nextAllowed() {
        return switch (this) {
            case PENDING -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(SHIPPING, CANCELLED);
            case SHIPPING -> EnumSet.of(COMPLETED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        return nextAllowed().contains(target);
    }
}
