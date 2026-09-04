package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.ComboPackageDto;
import com.tiffin.system.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboPackageDto>>> getAllCombos() {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getAllCombos()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ComboPackageDto>>> getActiveCombos() {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getActiveCombos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComboPackageDto>> getComboById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(comboService.getComboById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComboPackageDto>> createCombo(
            @Valid @RequestBody ComboPackageDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        ComboPackageDto created = comboService.createCombo(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Combo / Thali package created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComboPackageDto>> updateCombo(
            @PathVariable Long id,
            @Valid @RequestBody ComboPackageDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        ComboPackageDto updated = comboService.updateCombo(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Combo / Thali package updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteCombo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        comboService.deleteCombo(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Combo package deleted successfully", null));
    }
}