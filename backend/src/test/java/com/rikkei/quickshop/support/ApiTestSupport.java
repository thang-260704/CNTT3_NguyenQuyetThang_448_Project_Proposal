package com.rikkei.quickshop.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Hàm dùng chung cho các integration test gọi API qua MockMvc. */
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    protected String toJson(Object body) throws Exception {
        return json.writeValueAsString(body);
    }

    protected JsonNode read(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    protected String login(String username, String password) throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + read(res).get("token").asText();
    }

    protected String adminToken() throws Exception {
        return login("admin", "admin123");
    }

    /** Đăng ký một khách hàng mới (tên ngẫu nhiên) và trả về header Authorization. */
    protected String newCustomerToken() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", username, "email", username + "@test.local",
                                "password", "secret123", "fullName", "Test User"))))
                .andExpect(status().isCreated())
                .andReturn();
        return "Bearer " + read(res).get("token").asText();
    }

    /** Admin tạo sản phẩm với tồn kho xác định để test không phụ thuộc dữ liệu seed. */
    protected long createProduct(String name, long price, int stock) throws Exception {
        MvcResult res = mvc.perform(post("/api/admin/products")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", name, "price", price, "stock", stock,
                                "description", "test product"))))
                .andExpect(status().isCreated())
                .andReturn();
        return read(res).get("id").asLong();
    }
}
