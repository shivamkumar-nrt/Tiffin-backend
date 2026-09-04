package com.tiffin.system.service;

import com.tiffin.system.dto.ComboPackageDto;

import java.util.List;

public interface ComboService {
    List<ComboPackageDto> getAllCombos();
    List<ComboPackageDto> getActiveCombos();
    ComboPackageDto getComboById(Long id);
    ComboPackageDto createCombo(ComboPackageDto dto, String adminEmail);
    ComboPackageDto updateCombo(Long id, ComboPackageDto dto, String adminEmail);
    void deleteCombo(Long id, String adminEmail);
}