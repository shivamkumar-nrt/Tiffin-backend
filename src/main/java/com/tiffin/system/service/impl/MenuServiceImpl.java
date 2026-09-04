package com.tiffin.system.service.impl;

import com.tiffin.system.dto.MenuDto;
import com.tiffin.system.dto.MenuItemDto;
import com.tiffin.system.entity.Menu;
import com.tiffin.system.entity.MenuItem;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.MenuItemRepository;
import com.tiffin.system.repository.MenuRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "menus", key = "#date.toString()")
    public MenuDto getMenuByDate(LocalDate date) {
        return menuRepository.findByServiceDate(date)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public MenuDto createOrUpdateMenu(MenuDto menuDto, String adminEmail) {
        Optional<Menu> existingMenuOpt = menuRepository.findByServiceDate(menuDto.getServiceDate());
        Menu menu;

        if (existingMenuOpt.isPresent()) {
            menu = existingMenuOpt.get();
            menu.setTitle(menuDto.getTitle());
            menu.setDescription(menuDto.getDescription());
            menu.setSpecial(menuDto.isSpecial());
            menu.getItems().clear();
        } else {
            menu = Menu.builder()
                    .serviceDate(menuDto.getServiceDate())
                    .title(menuDto.getTitle())
                    .description(menuDto.getDescription())
                    .isSpecial(menuDto.isSpecial())
                    .build();
        }

        if (menuDto.getItems() != null) {
            for (MenuItemDto itemDto : menuDto.getItems()) {
                MenuItem item = MenuItem.builder()
                        .name(itemDto.getName())
                        .category(itemDto.getCategory())
                        .description(itemDto.getDescription())
                        .isSpicy(itemDto.isSpicy())
                        .isSweet(itemDto.isSweet())
                        .isAvailable(true)
                        .build();
                menu.addItem(item);
            }
        }

        Menu saved = menuRepository.save(menu);

        auditService.logAction("SAVE_MENU", "Menu", saved.getId().toString(), adminEmail,
                "Configured menu for date: " + saved.getServiceDate() + " ('" + saved.getTitle() + "')");

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getMenusInRange(LocalDate startDate, LocalDate endDate) {
        return menuRepository.findByServiceDateBetweenOrderByServiceDateAsc(startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public void deleteMenu(Long id, String adminEmail) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with id: " + id));
        LocalDate date = menu.getServiceDate();
        menuRepository.delete(menu);

        auditService.logAction("DELETE_MENU", "Menu", id.toString(), adminEmail, "Deleted menu for date: " + date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAllMenuItems() {
        return menuItemRepository.findAllByOrderByNameAsc().stream()
                .map(i -> MenuItemDto.builder()
                        .id(i.getId())
                        .name(i.getName())
                        .category(i.getCategory())
                        .description(i.getDescription())
                        .isSpicy(i.isSpicy())
                        .isSweet(i.isSweet())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuItemDto createMenuItem(MenuItemDto dto, String adminEmail) {
        MenuItem item = MenuItem.builder()
                .name(dto.getName().trim())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .isSpicy(dto.isSpicy())
                .isSweet(dto.isSweet())
                .isAvailable(true)
                .build();

        MenuItem saved = menuItemRepository.save(item);

        auditService.logAction("CREATE_MENU_ITEM", "MenuItem", saved.getId().toString(), adminEmail,
                "Added food dish/item: " + saved.getName() + " (" + saved.getCategory() + ")");

        return MenuItemDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .category(saved.getCategory())
                .description(saved.getDescription())
                .isSpicy(saved.isSpicy())
                .isSweet(saved.isSweet())
                .build();
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id, String adminEmail) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        menuItemRepository.delete(item);

        auditService.logAction("DELETE_MENU_ITEM", "MenuItem", id.toString(), adminEmail,
                "Deleted dish/item: " + item.getName());
    }

    private MenuDto mapToDto(Menu menu) {
        List<MenuItemDto> itemDtos = menu.getItems().stream().map(i -> MenuItemDto.builder()
                .id(i.getId())
                .name(i.getName())
                .category(i.getCategory())
                .description(i.getDescription())
                .isSpicy(i.isSpicy())
                .isSweet(i.isSweet())
                .build()
        ).collect(Collectors.toList());

        return MenuDto.builder()
                .id(menu.getId())
                .serviceDate(menu.getServiceDate())
                .title(menu.getTitle())
                .description(menu.getDescription())
                .isSpecial(menu.isSpecial())
                .items(itemDtos)
                .build();
    }
}