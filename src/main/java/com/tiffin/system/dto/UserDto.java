package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String department;
    private RoleType role;
    private UserStatus status;
    private BigDecimal outstandingBalance;
    private LocalDateTime createdAt;
}
