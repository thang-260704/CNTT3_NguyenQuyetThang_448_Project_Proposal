package com.rikkei.quickshop.dto;

import com.rikkei.quickshop.entity.Order;
import com.rikkei.quickshop.entity.OrderItem;
import com.rikkei.quickshop.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemRequest(
            @NotNull(message = "Thiếu mã sản phẩm") Long productId,
            @NotNull(message = "Thiếu số lượng")
            @Min(value = 1, message = "Số lượng tối thiểu là 1")
            @Max(value = 100, message = "Số lượng tối đa là 100") Integer quantity) {
    }

    public record CheckoutRequest(
            @NotBlank(message = "Họ tên không được để trống")
            @Size(max = 120, message = "Họ tên tối đa 120 ký tự") String fullName,
            @NotBlank(message = "Số điện thoại không được để trống")
            @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            @NotBlank(message = "Địa chỉ không được để trống")
            @Size(max = 300, message = "Địa chỉ tối đa 300 ký tự") String address,
            @NotEmpty(message = "Giỏ hàng đang trống")
            @Size(max = 50, message = "Tối đa 50 sản phẩm mỗi đơn") List<@Valid OrderItemRequest> items) {
    }

    public record UpdateStatusRequest(@NotNull(message = "Thiếu trạng thái") OrderStatus status) {
    }

    public record OrderItemResponse(Long productId, String productName, String imageUrl, BigDecimal unitPrice,
                                    Integer quantity, BigDecimal subtotal) {
        public static OrderItemResponse from(OrderItem i) {
            return new OrderItemResponse(i.getProduct() == null ? null : i.getProduct().getId(), i.getProductName(),
                    i.getImageUrl(), i.getUnitPrice(), i.getQuantity(), i.getSubtotal());
        }
    }

    public record OrderResponse(Long id, String username, String fullName, String phone, String address,
                                String paymentMethod, OrderStatus status, List<OrderStatus> nextStatuses,
                                BigDecimal totalAmount, List<OrderItemResponse> items,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getUser().getUsername(), o.getFullName(), o.getPhone(),
                    o.getAddress(), o.getPaymentMethod().name(), o.getStatus(),
                    o.getStatus().nextAllowed().stream().sorted().toList(), o.getTotalAmount(),
                    o.getItems().stream().map(OrderItemResponse::from).toList(), o.getCreatedAt(), o.getUpdatedAt());
        }
    }
}
