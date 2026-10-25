package com.rikkei.quickshop.dto;

import com.rikkei.quickshop.entity.User;

public record UserResponse(Long id, String username, String email, String fullName, String role) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getRole().name());
    }
}
