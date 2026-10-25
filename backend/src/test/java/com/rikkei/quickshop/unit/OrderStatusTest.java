package com.rikkei.quickshop.unit;

import com.rikkei.quickshop.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    @DisplayName("TC-UNIT-01: Quy tắc chuyển trạng thái đơn hàng")
    void transitions() {
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.PROCESSING)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.PENDING.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
        assertThat(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPING)).isTrue();
        assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.COMPLETED)).isTrue();
        assertThat(OrderStatus.COMPLETED.nextAllowed()).isEmpty();
        assertThat(OrderStatus.CANCELLED.nextAllowed()).isEmpty();
    }
}
