package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.dto.AuthDtos.AuthResponse;
import com.rikkei.quickshop.dto.AuthDtos.LoginRequest;
import com.rikkei.quickshop.dto.AuthDtos.RegisterRequest;
import com.rikkei.quickshop.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
