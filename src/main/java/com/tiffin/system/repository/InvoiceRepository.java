package com.tiffin.system.repository;

import com.tiffin.system.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByUserIdOrderByGeneratedAtDesc(Long userId);
    
    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE (:userId IS NULL OR i.user.id = :userId) " +
           "AND (:startDate IS NULL OR i.billingStartDate >= :startDate) " +
           "AND (:endDate IS NULL OR i.billingEndDate <= :endDate) " +
           "ORDER BY i.generatedAt DESC")
    Page<Invoice> searchInvoices(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );
}
