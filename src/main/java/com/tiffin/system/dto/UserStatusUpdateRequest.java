package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;
}
