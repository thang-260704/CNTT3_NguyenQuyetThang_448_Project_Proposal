package com.rikkei.quickshop;

import com.rikkei.quickshop.repository.UserRepository;
import com.rikkei.quickshop.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest extends ApiTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("TC-AUTH-01: Đăng ký thành công trả về JWT và role CUSTOMER")
    void registerSuccess() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "alice01", "email", "alice01@test.local",
                                "password", "secret123", "fullName", "Alice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    @DisplayName("TC-AUTH-02: Mật khẩu được lưu dạng BCrypt, không phải plaintext (NFR-04)")
    void passwordIsBcrypted() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "bob01", "email", "bob01@test.local",
                                "password", "secret123"))))
                .andExpect(status().isCreated());
        String stored = userRepository.findByUsername("bob01").orElseThrow().getPassword();
        assertThat(stored).isNotEqualTo("secret123").startsWith("$2a$");
    }

    @Test
    @DisplayName("TC-AUTH-03: Đăng ký trùng tên đăng nhập trả về 409")
    void registerDuplicate() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "admin", "email", "other@test.local",
                                "password", "secret123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Tên đăng nhập đã tồn tại"));
    }

    @Test
    @DisplayName("TC-AUTH-04: Đăng ký thiếu dữ liệu / email sai định dạng trả về 400 kèm lỗi từng trường")
    void registerValidation() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "x", "email", "not-an-email", "password", "1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("TC-AUTH-05: Đăng nhập đúng tài khoản admin trả về role ADMIN")
    void loginAdmin() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "admin", "password", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("TC-AUTH-06: Đăng nhập sai mật khẩu trả về 401")
    void loginWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "admin", "password", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai tên đăng nhập hoặc mật khẩu"));
    }

    @Test
    @DisplayName("TC-AUTH-07: Gọi API cần đăng nhập không có token / token giả trả về 401 (NFR-05)")
    void protectedWithoutToken() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-AUTH-08: Xem và cập nhật hồ sơ cá nhân")
    void profile() throws Exception {
        String token = newCustomerToken();
        mvc.perform(get("/api/users/me").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
        mvc.perform(put("/api/users/me").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "updated_" + System.nanoTime() + "@test.local",
                                "fullName", "Nguyễn Văn Test"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn Test"));
    }

    @Test
    @DisplayName("TC-AUTH-09: Đổi mật khẩu: sai mật khẩu cũ 400, đúng thì đăng nhập được bằng mật khẩu mới")
    void changePassword() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("username", "carol01", "email", "carol01@test.local",
                                "password", "secret123"))))
                .andExpect(status().isCreated());
        String token = login("carol01", "secret123");
        mvc.perform(put("/api/users/me/password").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("currentPassword", "wrong", "newPassword", "newsecret1"))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/users/me/password").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("currentPassword", "secret123", "newPassword", "newsecret1"))))
                .andExpect(status().isNoContent());
        login("carol01", "newsecret1");
    }
}
