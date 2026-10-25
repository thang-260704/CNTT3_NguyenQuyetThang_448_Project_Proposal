package com.rikkei.quickshop.dto;

import java.math.BigDecimal;
import java.util.Map;

public record StatsResponse(long totalProducts, long totalOrders, BigDecimal completedRevenue,
                            Map<String, Long> ordersByStatus) {
}
