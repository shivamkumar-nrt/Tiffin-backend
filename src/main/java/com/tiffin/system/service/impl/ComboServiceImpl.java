package com.tiffin.system.service.impl;

import com.tiffin.system.dto.ComboPackageDto;
import com.tiffin.system.entity.ComboPackage;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.ComboPackageRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {

    private final ComboPackageRepository comboPackageRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<ComboPackageDto> getAllCombos() {
        return comboPackageRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboPackageDto> getActiveCombos() {
        return comboPackageRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComboPackageDto getComboById(Long id) {
        return comboPackageRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + id));
    }

    @Override
    @Transactional
    public ComboPackageDto createCombo(ComboPackageDto dto, String adminEmail) {
        ComboPackage combo = ComboPackage.builder()
                .name(dto.getName().trim())
                .tiffinType(dto.getTiffinType())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .includedItems(dto.getIncludedItems() != null ? new ArrayList<>(dto.getIncludedItems()) : new ArrayList<>())
                .isActive(dto.isActive())
                .build();

        ComboPackage saved = comboPackageRepository.save(combo);

        auditService.logAction("CREATE_COMBO", "ComboPackage", saved.getId().toString(), adminEmail,
                "Created combo/thali package: " + saved.getName() + " (Rs. " + saved.getPrice() + ")");

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ComboPackageDto updateCombo(Long id, ComboPackageDto dto, String adminEmail) {
        ComboPackage combo = comboPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + id));

        combo.setName(dto.getName().trim());
        combo.setTiffinType(dto.getTiffinType());
        combo.setPrice(dto.getPrice());
        combo.setDescription(dto.getDescription());
        combo.setIncludedItems(dto.getIncludedItems() != null ? new ArrayList<>(dto.getIncludedItems()) : new ArrayList<>());
        combo.setActive(dto.isActive());

        ComboPackage updated = comboPackageRepository.save(combo);

        auditService.logAction("UPDATE_COMBO", "ComboPackage", id.toString(), adminEmail,
                "Updated combo/thali package: " + updated.getName() + " (Rs. " + updated.getPrice() + ")");

        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteCombo(Long id, String adminEmail) {
        ComboPackage combo = comboPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Combo package not found with id: " + id));

        comboPackageRepository.delete(combo);

        auditService.logAction("DELETE_COMBO", "ComboPackage", id.toString(), adminEmail,
                "Deleted combo package: " + combo.getName());
    }

    private ComboPackageDto mapToDto(ComboPackage c) {
        return ComboPackageDto.builder()
                .id(c.getId())
                .name(c.getName())
                .tiffinType(c.getTiffinType())
                .price(c.getPrice())
                .description(c.getDescription())
                .includedItems(c.getIncludedItems() != null ? new ArrayList<>(c.getIncludedItems()) : new ArrayList<>())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .build();
    }
}