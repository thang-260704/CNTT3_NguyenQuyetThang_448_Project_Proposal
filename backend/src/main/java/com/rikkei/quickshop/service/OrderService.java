package com.rikkei.quickshop.service;

import com.rikkei.quickshop.dto.OrderDtos.CheckoutRequest;
import com.rikkei.quickshop.dto.OrderDtos.OrderItemRequest;
import com.rikkei.quickshop.dto.OrderDtos.OrderResponse;
import com.rikkei.quickshop.dto.PageResponse;
import com.rikkei.quickshop.dto.StatsResponse;
import com.rikkei.quickshop.entity.*;
import com.rikkei.quickshop.exception.ApiException;
import com.rikkei.quickshop.repository.OrderRepository;
import com.rikkei.quickshop.repository.ProductRepository;
import com.rikkei.quickshop.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Đặt hàng COD: kiểm tra và trừ tồn kho trong cùng một transaction.
     * Sản phẩm được khóa (PESSIMISTIC_WRITE) theo thứ tự id tăng dần để tránh deadlock khi đặt đồng thời.
     */
    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));

        Map<Long, Integer> quantities = new TreeMap<>();
        for (OrderItemRequest item : req.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        Order order = new Order();
        order.setUser(user);
        order.setFullName(req.fullName().trim());
        order.setPhone(req.phone().trim());
        order.setAddress(req.address().trim());
        order.setPaymentMethod(PaymentMethod.COD);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = productRepository.findByIdForUpdate(entry.getKey())
                    .filter(Product::isActive)
                    .orElseThrow(() -> ApiException.badRequest("Sản phẩm #" + entry.getKey() + " không tồn tại"));
            int qty = entry.getValue();
            if (product.getStock() < qty) {
                throw ApiException.conflict("Sản phẩm \"" + product.getName() + "\" chỉ còn "
                        + product.getStock() + " trong kho");
            }
            product.setStock(product.getStock() - qty);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setImageUrl(product.getImageUrl());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(qty);
            order.addItem(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
        log.info("Order #{} created by {} total={}", order.getId(), user.getUsername(), total);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse myOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng"));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> allOrders(OrderStatus status, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "id"));
        Page<Order> result = status == null ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.of(result, OrderResponse::from);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus target) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy đơn hàng"));
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(target)) {
            throw ApiException.badRequest("Không thể chuyển trạng thái từ " + current + " sang " + target);
        }
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(target);
        log.info("Order #{} status {} -> {}", orderId, current, target);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s.name(), orderRepository.countByStatus(s));
        }
        BigDecimal revenue = orderRepository.findByStatus(OrderStatus.COMPLETED, PageRequest.of(0, 10_000))
                .stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsResponse(productRepository.countByActiveTrue(), orderRepository.count(), revenue, byStatus);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                productRepository.findByIdForUpdate(item.getProduct().getId())
                        .ifPresent(p -> p.setStock(p.getStock() + item.getQuantity()));
            }
        }
    }
}
