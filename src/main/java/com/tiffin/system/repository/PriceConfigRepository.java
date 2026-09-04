package com.tiffin.system.repository;

import com.tiffin.system.entity.PriceConfig;
import com.tiffin.system.entity.enums.TiffinType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {
    
    @Query("SELECT p FROM PriceConfig p WHERE p.tiffinType = :type AND p.isActive = true AND p.effectiveFrom <= :date AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date) ORDER BY p.effectiveFrom DESC")
    List<PriceConfig> findActivePriceByTypeAndDate(@Param("type") TiffinType type, @Param("date") LocalDate date);

    default Optional<PriceConfig> findCurrentPrice(TiffinType type, LocalDate date) {
        List<PriceConfig> list = findActivePriceByTypeAndDate(type, date);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    List<PriceConfig> findAllByOrderByEffectiveFromDesc();
}
