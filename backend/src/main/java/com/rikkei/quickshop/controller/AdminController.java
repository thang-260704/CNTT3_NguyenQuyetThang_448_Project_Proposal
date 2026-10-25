package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.ai.AiService;
import com.rikkei.quickshop.ai.MockDataService;
import com.rikkei.quickshop.dto.AiDtos.DescriptionRequest;
import com.rikkei.quickshop.dto.AiDtos.DescriptionResponse;
import com.rikkei.quickshop.dto.OrderDtos.OrderResponse;
import com.rikkei.quickshop.dto.OrderDtos.UpdateStatusRequest;
import com.rikkei.quickshop.dto.PageResponse;
import com.rikkei.quickshop.dto.ProductDtos.ProductRequest;
import com.rikkei.quickshop.dto.ProductDtos.ProductResponse;
import com.rikkei.quickshop.dto.StatsResponse;
import com.rikkei.quickshop.entity.OrderStatus;
import com.rikkei.quickshop.service.OrderService;
import com.rikkei.quickshop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Toàn bộ endpoint /api/admin/** yêu cầu ROLE_ADMIN (cấu hình trong SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final AiService aiService;
    private final MockDataService mockDataService;

    public AdminController(ProductService productService, OrderService orderService, AiService aiService,
                           MockDataService mockDataService) {
        this.productService = productService;
        this.orderService = orderService;
        this.aiService = aiService;
        this.mockDataService = mockDataService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest req) {
        return productService.create(req);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest req) {
        return productService.update(id, req);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> orders(@RequestParam(required = false) OrderStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return orderService.allOrders(status, page, size);
    }

    @PatchMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        return orderService.updateStatus(id, req.status());
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return orderService.stats();
    }

    @PostMapping("/ai/description")
    public DescriptionResponse aiDescription(@Valid @RequestBody DescriptionRequest req) {
        return aiService.generateDescription(req.name(), req.category());
    }

    @PostMapping("/ai/mock-products")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProductResponse> mockProducts(@RequestParam(defaultValue = "5") int count) {
        return mockDataService.generate(count);
    }
}
