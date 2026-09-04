package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.MenuDto;
import com.tiffin.system.dto.MenuItemDto;
import com.tiffin.system.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<MenuDto>> getMenuByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        MenuDto menu = menuService.getMenuByDate(targetDate);
        return ResponseEntity.ok(ApiResponse.ok(menu));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<MenuDto>>> getMenusInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MenuDto> menus = menuService.getMenusInRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(menus));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuDto>> createOrUpdateMenu(
            @Valid @RequestBody MenuDto menuDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        MenuDto saved = menuService.createOrUpdateMenu(menuDto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Menu saved successfully", saved));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMenu(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        menuService.deleteMenu(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Menu deleted successfully", null));
    }

    // Dish Items Catalog Endpoints
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<MenuItemDto>>> getAllMenuItems() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getAllMenuItems()));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemDto>> createMenuItem(
            @Valid @RequestBody MenuItemDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        MenuItemDto saved = menuService.createMenuItem(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Menu dish item added successfully", saved));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteMenuItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        menuService.deleteMenuItem(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Menu dish item deleted successfully", null));
    }
}