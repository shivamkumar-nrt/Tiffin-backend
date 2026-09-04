package com.tiffin.system.service;

import com.tiffin.system.dto.PriceConfigDto;
import com.tiffin.system.entity.PriceConfig;
import com.tiffin.system.entity.enums.TiffinType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PriceService {
    BigDecimal getPriceForTypeAndDate(TiffinType type, LocalDate date);
    Map<String, PriceConfigDto> getCurrentPrices();
    List<PriceConfigDto> getAllPrices();
    PriceConfigDto createPriceConfig(PriceConfigDto dto, String adminEmail);
    PriceConfigDto updatePriceConfig(Long id, PriceConfigDto dto, String adminEmail);
}
