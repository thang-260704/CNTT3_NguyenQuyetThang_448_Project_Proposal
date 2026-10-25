package com.rikkei.quickshop.service;

import com.rikkei.quickshop.dto.AuthDtos.AuthResponse;
import com.rikkei.quickshop.dto.AuthDtos.LoginRequest;
import com.rikkei.quickshop.dto.AuthDtos.RegisterRequest;
import com.rikkei.quickshop.dto.UserResponse;
import com.rikkei.quickshop.entity.Role;
import com.rikkei.quickshop.entity.User;
import com.rikkei.quickshop.exception.ApiException;
import com.rikkei.quickshop.repository.UserRepository;
import com.rikkei.quickshop.security.AppUserDetails;
import com.rikkei.quickshop.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email đã được sử dụng");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(req.fullName() == null ? null : req.fullName().trim());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        log.info("New customer registered: {}", username);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username().trim(), req.password()));
        AppUserDetails principal = (AppUserDetails) auth.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy người dùng"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String token = jwtService.generateToken(new AppUserDetails(user));
        return new AuthResponse(token, "Bearer", jwtService.getExpirationMs() / 1000, UserResponse.from(user));
    }
}
