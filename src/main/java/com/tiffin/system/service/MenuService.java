package com.tiffin.system.service;

import com.tiffin.system.dto.MenuDto;
import com.tiffin.system.dto.MenuItemDto;

import java.time.LocalDate;
import java.util.List;

public interface MenuService {
    MenuDto getMenuByDate(LocalDate date);
    MenuDto createOrUpdateMenu(MenuDto menuDto, String adminEmail);
    List<MenuDto> getMenusInRange(LocalDate startDate, LocalDate endDate);
    void deleteMenu(Long id, String adminEmail);

    // Master dishes/items management
    List<MenuItemDto> getAllMenuItems();
    MenuItemDto createMenuItem(MenuItemDto dto, String adminEmail);
    void deleteMenuItem(Long id, String adminEmail);
}