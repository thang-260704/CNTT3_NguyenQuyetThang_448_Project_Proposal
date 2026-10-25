package com.rikkei.quickshop.dto;

import com.rikkei.quickshop.entity.Category;
import com.rikkei.quickshop.entity.Product;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record CategoryResponse(Long id, String name) {
        public static CategoryResponse from(Category c) {
            return c == null ? null : new CategoryResponse(c.getId(), c.getName());
        }
    }

    public record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stock,
                                  String imageUrl, CategoryResponse category) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                    p.getImageUrl(), CategoryResponse.from(p.getCategory()));
        }
    }

    public record ProductRequest(
            @NotBlank(message = "Tên sản phẩm không được để trống")
            @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự") String name,
            @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự") String description,
            @NotNull(message = "Giá không được để trống")
            @DecimalMin(value = "0", message = "Giá không được âm") BigDecimal price,
            @NotNull(message = "Tồn kho không được để trống")
            @Min(value = 0, message = "Tồn kho không được âm") Integer stock,
            @Size(max = 500, message = "URL ảnh tối đa 500 ký tự") String imageUrl,
            Long categoryId) {
    }
}
