package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private RoleType role;
    private BigDecimal outstandingBalance;
}
