package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.ai.AiService;
import com.rikkei.quickshop.dto.AiDtos.ChatRequest;
import com.rikkei.quickshop.dto.AiDtos.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        return aiService.chat(req.message());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("mode", aiService.isLlmEnabled() ? "llm" : "fallback");
    }
}
