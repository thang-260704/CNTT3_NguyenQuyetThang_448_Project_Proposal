package com.rikkei.quickshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AiDtos {

    private AiDtos() {
    }

    public record DescriptionRequest(
            @NotBlank(message = "Vui lòng nhập tên sản phẩm") @Size(max = 200) String name,
            @Size(max = 80) String category) {
    }

    public record DescriptionResponse(String description, String source) {
    }

    public record ChatRequest(@NotBlank(message = "Vui lòng nhập câu hỏi") @Size(max = 500) String message) {
    }

    public record ChatResponse(String reply, List<ProductDtos.ProductResponse> suggestions, String source) {
    }
}
