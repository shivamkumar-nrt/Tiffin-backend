package com.tiffin.system.service.impl;

import com.tiffin.system.dto.PriceConfigDto;
import com.tiffin.system.entity.PriceConfig;
import com.tiffin.system.entity.enums.TiffinType;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.PriceConfigRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceConfigRepository priceConfigRepository;
    private final AuditService auditService;

    @Value("${tiffin.app.defaultFullPrice:120.00}")
    private BigDecimal defaultFullPrice;

    @Value("${tiffin.app.defaultHalfPrice:80.00}")
    private BigDecimal defaultHalfPrice;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPriceForTypeAndDate(TiffinType type, LocalDate date) {
        return priceConfigRepository.findCurrentPrice(type, date)
                .map(PriceConfig::getPrice)
                .orElse(type == TiffinType.FULL ? defaultFullPrice : defaultHalfPrice);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "prices", key = "'current'")
    public Map<String, PriceConfigDto> getCurrentPrices() {
        LocalDate today = LocalDate.now();
        Map<String, PriceConfigDto> result = new HashMap<>();

        PriceConfig full = priceConfigRepository.findCurrentPrice(TiffinType.FULL, today)
                .orElse(PriceConfig.builder().tiffinType(TiffinType.FULL).price(defaultFullPrice).effectiveFrom(today).isActive(true).build());
        
        PriceConfig half = priceConfigRepository.findCurrentPrice(TiffinType.HALF, today)
                .orElse(PriceConfig.builder().tiffinType(TiffinType.HALF).price(defaultHalfPrice).effectiveFrom(today).isActive(true).build());

        result.put("FULL", mapToDto(full));
        result.put("HALF", mapToDto(half));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceConfigDto> getAllPrices() {
        return priceConfigRepository.findAllByOrderByEffectiveFromDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "prices", allEntries = true)
    public PriceConfigDto createPriceConfig(PriceConfigDto dto, String adminEmail) {
        PriceConfig priceConfig = PriceConfig.builder()
                .tiffinType(dto.getTiffinType())
                .price(dto.getPrice())
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .isActive(dto.isActive())
                .build();

        PriceConfig saved = priceConfigRepository.save(priceConfig);

        auditService.logAction("CREATE_PRICE", "PriceConfig", saved.getId().toString(), adminEmail,
                "Set " + saved.getTiffinType() + " price to Rs. " + saved.getPrice() + " effective from " + saved.getEffectiveFrom());

        return mapToDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "prices", allEntries = true)
    public PriceConfigDto updatePriceConfig(Long id, PriceConfigDto dto, String adminEmail) {
        PriceConfig config = priceConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Price config not found with id: " + id));

        config.setPrice(dto.getPrice());
        config.setEffectiveFrom(dto.getEffectiveFrom());
        config.setEffectiveTo(dto.getEffectiveTo());
        config.setActive(dto.isActive());

        PriceConfig updated = priceConfigRepository.save(config);

        auditService.logAction("UPDATE_PRICE", "PriceConfig", id.toString(), adminEmail,
                "Updated " + updated.getTiffinType() + " price to Rs. " + updated.getPrice());

        return mapToDto(updated);
    }

    private PriceConfigDto mapToDto(PriceConfig config) {
        return PriceConfigDto.builder()
                .id(config.getId())
                .tiffinType(config.getTiffinType())
                .price(config.getPrice())
                .effectiveFrom(config.getEffectiveFrom())
                .effectiveTo(config.getEffectiveTo())
                .isActive(config.isActive())
                .build();
    }
}
