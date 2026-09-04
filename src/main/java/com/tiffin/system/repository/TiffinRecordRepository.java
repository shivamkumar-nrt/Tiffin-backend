package com.tiffin.system.repository;

import com.tiffin.system.entity.TiffinRecord;
import com.tiffin.system.entity.enums.RecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TiffinRecordRepository extends JpaRepository<TiffinRecord, Long> {
    
    List<TiffinRecord> findByUserIdOrderByServiceDateDesc(Long userId);
    
    List<TiffinRecord> findByUserIdAndStatus(Long userId, RecordStatus status);
    
    List<TiffinRecord> findByServiceDateOrderByCreatedAtDesc(LocalDate serviceDate);

    @Query("SELECT COALESCE(SUM(r.chargedAmount), 0) FROM TiffinRecord r WHERE r.user.id = :userId AND r.status = :status")
    BigDecimal sumAmountByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RecordStatus status);

    @Query("SELECT r FROM TiffinRecord r WHERE (:userId IS NULL OR r.user.id = :userId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:startDate IS NULL OR r.serviceDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.serviceDate <= :endDate) " +
           "ORDER BY r.serviceDate DESC")
    Page<TiffinRecord> searchRecords(
        @Param("userId") Long userId,
        @Param("status") RecordStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    long countByServiceDate(LocalDate serviceDate);
    
    @Query("SELECT COALESCE(SUM(r.chargedAmount), 0) FROM TiffinRecord r WHERE r.serviceDate = :serviceDate")
    BigDecimal sumChargedAmountByServiceDate(@Param("serviceDate") LocalDate serviceDate);
}
