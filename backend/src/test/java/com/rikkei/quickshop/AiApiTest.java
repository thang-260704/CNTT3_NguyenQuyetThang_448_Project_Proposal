package com.rikkei.quickshop;

import com.rikkei.quickshop.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AiApiTest extends ApiTestSupport {

    @Test
    @DisplayName("TC-AI-01: Admin sinh mô tả sản phẩm bằng AI (FR-05)")
    void aiDescription() throws Exception {
        mvc.perform(post("/api/admin/ai/description").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Tai nghe gaming X1", "category", "Điện tử"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", containsString("Tai nghe gaming X1")))
                .andExpect(jsonPath("$.source").value("fallback"));
    }

    @Test
    @DisplayName("TC-AI-02: Khách hàng không dùng được AI sinh mô tả của admin (403)")
    void aiDescriptionForbidden() throws Exception {
        mvc.perform(post("/api/admin/ai/description").header("Authorization", newCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("name", "X"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-AI-03: Chatbot gợi ý đúng sản phẩm theo từ khóa, không cần đăng nhập")
    void chatbotKeyword() throws Exception {
        mvc.perform(post("/api/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("message", "Shop có tai nghe nào không?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply", not(emptyString())))
                .andExpect(jsonPath("$.suggestions", not(empty())))
                .andExpect(jsonPath("$.suggestions[0].name", containsStringIgnoringCase("tai nghe")));
    }

    @Test
    @DisplayName("TC-AI-04: Chatbot lọc theo khoảng giá (dưới 200k)")
    void chatbotPrice() throws Exception {
        mvc.perform(post("/api/ai/chat").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("message", "gợi ý sản phẩm dưới 200k"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions", not(empty())))
                .andExpect(jsonPath("$.suggestions[*].price", everyItem(lessThanOrEqualTo(200000))));
    }

    @Test
    @DisplayName("TC-AI-05: Admin tạo mock data sản phẩm bằng AI")
    void mockData() throws Exception {
        mvc.perform(post("/api/admin/ai/mock-products").param("count", "3").header("Authorization", adminToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].description", not(emptyString())));
    }
}
