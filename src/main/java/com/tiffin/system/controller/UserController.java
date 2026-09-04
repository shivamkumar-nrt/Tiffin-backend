package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.RegisterRequest;
import com.tiffin.system.dto.UserDto;
import com.tiffin.system.dto.UserStatusUpdateRequest;
import com.tiffin.system.service.AuthService;
import com.tiffin.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<UserDto> users = userService.searchUsers(search, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.ok(users));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAllEmployees()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto userDto = authService.register(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User created successfully", userDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(authentication, #id)")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDto updated = userService.updateUserStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("User status updated", updated));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetUserPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Password must be at least 6 characters"));
        }
        userService.resetUserPassword(id, newPassword, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Password updated successfully", null));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getUserBalance(@PathVariable Long id) {
        BigDecimal balance = userService.getUserOutstandingBalance(id);
        return ResponseEntity.ok(ApiResponse.ok(balance));
    }
}