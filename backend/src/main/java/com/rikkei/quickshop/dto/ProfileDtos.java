package com.rikkei.quickshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProfileDtos {

    private ProfileDtos() {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,
            @Size(max = 120, message = "Họ tên tối đa 120 ký tự") String fullName) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại") String currentPassword,
            @NotBlank(message = "Vui lòng nhập mật khẩu mới")
            @Size(min = 6, max = 100, message = "Mật khẩu mới tối thiểu 6 ký tự") String newPassword) {
    }
}
