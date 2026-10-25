package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.dto.OrderDtos.CheckoutRequest;
import com.rikkei.quickshop.dto.OrderDtos.OrderResponse;
import com.rikkei.quickshop.security.CurrentUser;
import com.rikkei.quickshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest req) {
        return orderService.checkout(CurrentUser.id(), req);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders() {
        return orderService.myOrders(CurrentUser.id());
    }

    @GetMapping("/{id}")
    public OrderResponse myOrder(@PathVariable Long id) {
        return orderService.myOrder(CurrentUser.id(), id);
    }
}
