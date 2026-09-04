package com.tiffin.system.repository;

import com.tiffin.system.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByServiceDate(LocalDate serviceDate);
    List<Menu> findByServiceDateBetweenOrderByServiceDateAsc(LocalDate startDate, LocalDate endDate);
    Boolean existsByServiceDate(LocalDate serviceDate);
}
