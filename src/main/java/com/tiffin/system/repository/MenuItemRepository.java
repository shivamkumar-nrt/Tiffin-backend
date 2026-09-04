package com.tiffin.system.repository;

import com.tiffin.system.entity.MenuItem;
import com.tiffin.system.entity.enums.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory(ItemCategory category);
    List<MenuItem> findByIsAvailableTrue();
    List<MenuItem> findAllByOrderByNameAsc();
}