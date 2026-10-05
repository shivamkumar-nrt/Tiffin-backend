package com.tiffin.system.dto;

import com.tiffin.system.entity.enums.RequestStatus;
import com.tiffin.system.entity.enums.TiffinType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTiffinRequestRequest {
    private LocalDate serviceDate;
    private TiffinType tiffinType;
    private String specialInstructions;
    private RequestStatus status;
}
