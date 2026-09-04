package com.tiffin.system.repository;

import com.tiffin.system.entity.ComboPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboPackageRepository extends JpaRepository<ComboPackage, Long> {
    List<ComboPackage> findByIsActiveTrue();
    List<ComboPackage> findAllByOrderByCreatedAtDesc();
}