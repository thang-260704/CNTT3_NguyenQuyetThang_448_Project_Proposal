package com.rikkei.quickshop.service;

import com.rikkei.quickshop.dto.ProfileDtos.ChangePasswordRequest;
import com.rikkei.quickshop.dto.ProfileDtos.UpdateProfileRequest;
import com.rikkei.quickshop.dto.UserResponse;
import com.rikkei.quickshop.entity.User;
import com.rikkei.quickshop.exception.ApiException;
import com.rikkei.quickshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(find(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = find(userId);
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw ApiException.conflict("Email đã được sử dụng");
        }
        user.setEmail(email);
        user.setFullName(req.fullName() == null ? null : req.fullName().trim());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = find(userId);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw ApiException.badRequest("Mật khẩu hiện tại không đúng");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
    }

    private User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));
    }
}
