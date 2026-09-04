package com.tiffin.system.controller;

import com.tiffin.system.dto.*;
import com.tiffin.system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        String creator = userDetails != null ? userDetails.getUsername() : null;
        UserDto userDto = authService.register(registerRequest, creator);
        return ResponseEntity.ok(ApiResponse.ok("User registered successfully", userDto));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDto profile = authService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
