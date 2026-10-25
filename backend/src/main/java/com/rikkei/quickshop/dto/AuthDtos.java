package com.rikkei.quickshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Tên đăng nhập không được để trống")
            @Size(min = 3, max = 50, message = "Tên đăng nhập dài 3-50 ký tự")
            @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "Tên đăng nhập chỉ gồm chữ, số, dấu _ và .")
            String username,
            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,
            @NotBlank(message = "Mật khẩu không được để trống")
            @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
            String password,
            @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
            String fullName) {
    }

    public record LoginRequest(
            @NotBlank(message = "Tên đăng nhập không được để trống") String username,
            @NotBlank(message = "Mật khẩu không được để trống") String password) {
    }

    public record AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {
    }
}
