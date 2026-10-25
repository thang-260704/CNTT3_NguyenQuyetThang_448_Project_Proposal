package com.rikkei.quickshop.controller;

import com.rikkei.quickshop.dto.ProfileDtos.ChangePasswordRequest;
import com.rikkei.quickshop.dto.ProfileDtos.UpdateProfileRequest;
import com.rikkei.quickshop.dto.UserResponse;
import com.rikkei.quickshop.security.CurrentUser;
import com.rikkei.quickshop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me() {
        return userService.getProfile(CurrentUser.id());
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateProfile(CurrentUser.id(), req);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(CurrentUser.id(), req);
        return ResponseEntity.noContent().build();
    }
}
