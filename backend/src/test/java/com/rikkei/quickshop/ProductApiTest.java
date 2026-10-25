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
class ProductApiTest extends ApiTestSupport {

    @Test
    @DisplayName("TC-PROD-01: Khách vãng lai xem danh sách sản phẩm có phân trang")
    void listPublic() throws Exception {
        mvc.perform(get("/api/products").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(16)))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].price").exists())
                .andExpect(jsonPath("$.content[0].stock").isNumber());
    }

    @Test
    @DisplayName("TC-PROD-02: Tìm kiếm theo từ khóa không phân biệt hoa thường")
    void searchKeyword() throws Exception {
        mvc.perform(get("/api/products").param("keyword", "TAI NGHE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].name", everyItem(containsStringIgnoringCase("tai nghe"))));
    }

    @Test
    @DisplayName("TC-PROD-03: Lọc sản phẩm theo danh mục")
    void filterCategory() throws Exception {
        String body = mvc.perform(get("/api/categories")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andReturn().getResponse().getContentAsString();
        long categoryId = json.readTree(body).get(0).get("id").asLong();
        mvc.perform(get("/api/products").param("categoryId", String.valueOf(categoryId)).param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].category.id", everyItem(is((int) categoryId))));
    }

    @Test
    @DisplayName("TC-PROD-04: Xem chi tiết sản phẩm; id không tồn tại trả về 404")
    void detail() throws Exception {
        long id = createProduct("Chi tiết test", 100_000, 5);
        mvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chi tiết test"));
        mvc.perform(get("/api/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-PROD-05: Khách hàng gọi API admin bị từ chối 403; chưa đăng nhập 401")
    void adminOnly() throws Exception {
        String body = toJson(Map.of("name", "Hack", "price", 1000, "stock", 1));
        mvc.perform(post("/api/admin/products").header("Authorization", newCustomerToken())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-PROD-06: Admin thêm - sửa - xóa sản phẩm (FR-04)")
    void adminCrud() throws Exception {
        String admin = adminToken();
        long id = createProduct("Sản phẩm CRUD", 250_000, 10);

        mvc.perform(put("/api/admin/products/" + id).header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "Sản phẩm CRUD v2", "price", 300_000, "stock", 7))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sản phẩm CRUD v2"))
                .andExpect(jsonPath("$.price").value(300000))
                .andExpect(jsonPath("$.stock").value(7));

        mvc.perform(delete("/api/admin/products/" + id).header("Authorization", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("TC-PROD-07: Admin tạo sản phẩm với giá âm bị từ chối 400")
    void adminValidation() throws Exception {
        mvc.perform(post("/api/admin/products").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", "", "price", -1, "stock", -5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stock").exists());
    }
}
