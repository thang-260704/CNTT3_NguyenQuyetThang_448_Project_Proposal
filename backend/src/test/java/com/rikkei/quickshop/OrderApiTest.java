package com.rikkei.quickshop;

import com.rikkei.quickshop.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest extends ApiTestSupport {

    private String checkoutBody(long productId, int qty) throws Exception {
        return toJson(Map.of("fullName", "Nguyễn Văn A", "phone", "0912345678", "address", "Hà Nội",
                "items", List.of(Map.of("productId", productId, "quantity", qty))));
    }

    private long placeOrder(String token, long productId, int qty) throws Exception {
        String res = mvc.perform(post("/api/orders").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(checkoutBody(productId, qty)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(res).get("id").asLong();
    }

    @Test
    @DisplayName("TC-ORD-01: Đặt hàng COD tạo đơn PENDING, tính đúng tổng tiền và trừ tồn kho (FR-03)")
    void checkoutDecrementsStock() throws Exception {
        long productId = createProduct("Sản phẩm đặt hàng", 120_000, 10);
        String token = newCustomerToken();

        mvc.perform(post("/api/orders").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(checkoutBody(productId, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.totalAmount").value(360000))
                .andExpect(jsonPath("$.items[0].quantity").value(3));

        mvc.perform(get("/api/products/" + productId)).andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    @DisplayName("TC-ORD-02: Đặt vượt tồn kho trả về 409 và không trừ kho")
    void checkoutOutOfStock() throws Exception {
        long productId = createProduct("Sản phẩm sắp hết", 50_000, 2);
        mvc.perform(post("/api/orders").header("Authorization", newCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON).content(checkoutBody(productId, 5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("chỉ còn 2")));
        mvc.perform(get("/api/products/" + productId)).andExpect(jsonPath("$.stock").value(2));
    }

    @Test
    @DisplayName("TC-ORD-03: Chưa đăng nhập không đặt hàng được (401)")
    void checkoutRequiresLogin() throws Exception {
        long productId = createProduct("Sản phẩm cần login", 50_000, 2);
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(checkoutBody(productId, 1)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-ORD-04: Checkout thiếu thông tin / số điện thoại sai / giỏ rỗng trả về 400")
    void checkoutValidation() throws Exception {
        mvc.perform(post("/api/orders").header("Authorization", newCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("fullName", "", "phone", "abc", "address", "", "items", List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.fullName").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.items").exists());
    }

    @Test
    @DisplayName("TC-ORD-05: Khách chỉ xem được lịch sử đơn của chính mình")
    void orderHistoryIsolation() throws Exception {
        long productId = createProduct("Sản phẩm lịch sử", 80_000, 10);
        String alice = newCustomerToken();
        String bob = newCustomerToken();
        long orderId = placeOrder(alice, productId, 1);

        mvc.perform(get("/api/orders/my").header("Authorization", alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(orderId));
        mvc.perform(get("/api/orders/my").header("Authorization", bob))
                .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/orders/" + orderId).header("Authorization", bob))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-ORD-06: Admin cập nhật trạng thái PENDING → PROCESSING → SHIPPING → COMPLETED (FR-06)")
    void adminStatusFlow() throws Exception {
        long productId = createProduct("Sản phẩm luồng trạng thái", 90_000, 10);
        long orderId = placeOrder(newCustomerToken(), productId, 1);
        String admin = adminToken();

        for (String next : List.of("PROCESSING", "SHIPPING", "COMPLETED")) {
            mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("status", next))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }
        mvc.perform(get("/api/admin/orders").header("Authorization", admin).param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) orderId)));
    }

    @Test
    @DisplayName("TC-ORD-07: Chuyển trạng thái sai quy trình (PENDING → COMPLETED) trả về 400")
    void adminInvalidTransition() throws Exception {
        long productId = createProduct("Sản phẩm sai trạng thái", 90_000, 10);
        long orderId = placeOrder(newCustomerToken(), productId, 1);
        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("status", "COMPLETED"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-ORD-08: Hủy đơn (CANCELLED) hoàn lại tồn kho")
    void cancelRestoresStock() throws Exception {
        long productId = createProduct("Sản phẩm hủy đơn", 70_000, 5);
        long orderId = placeOrder(newCustomerToken(), productId, 4);
        mvc.perform(get("/api/products/" + productId)).andExpect(jsonPath("$.stock").value(1));

        mvc.perform(patch("/api/admin/orders/" + orderId + "/status").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON).content(toJson(Map.of("status", "CANCELLED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/api/products/" + productId)).andExpect(jsonPath("$.stock").value(5));
    }

    @Test
    @DisplayName("TC-ORD-09: Khách hàng không gọi được API quản lý đơn của admin (403)")
    void customerCannotManageOrders() throws Exception {
        mvc.perform(get("/api/admin/orders").header("Authorization", newCustomerToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-ORD-10: Admin xem thống kê tổng quan")
    void adminStats() throws Exception {
        mvc.perform(get("/api/admin/stats").header("Authorization", adminToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts", greaterThan(0)))
                .andExpect(jsonPath("$.ordersByStatus.PENDING").exists());
    }
}
