package com.tiffin.system.controller;

import com.tiffin.system.dto.ApiResponse;
import com.tiffin.system.dto.PriceConfigDto;
import com.tiffin.system.service.PriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, PriceConfigDto>>> getCurrentPrices() {
        return ResponseEntity.ok(ApiResponse.ok(priceService.getCurrentPrices()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PriceConfigDto>>> getAllPrices() {
        return ResponseEntity.ok(ApiResponse.ok(priceService.getAllPrices()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceConfigDto>> createPrice(
            @Valid @RequestBody PriceConfigDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PriceConfigDto saved = priceService.createPriceConfig(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Price configuration created", saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PriceConfigDto>> updatePrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceConfigDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PriceConfigDto updated = priceService.updatePriceConfig(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Price configuration updated", updated));
    }
}
