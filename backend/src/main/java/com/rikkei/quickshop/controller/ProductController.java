package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.dto.PageResponse;
import com.rikkei.quickshop.dto.ProductDtos.CategoryResponse;
import com.rikkei.quickshop.dto.ProductDtos.ProductResponse;
import com.rikkei.quickshop.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public PageResponse<ProductResponse> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Long categoryId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "12") int size,
                                              @RequestParam(required = false) String sort) {
        return productService.search(keyword, categoryId, page, size, sort);
    }

    @GetMapping("/products/{id}")
    public ProductResponse detail(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return productService.categories();
    }
}
