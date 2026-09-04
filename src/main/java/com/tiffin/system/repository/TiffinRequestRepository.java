package com.tiffin.system.repository;

import com.tiffin.system.entity.TiffinRequest;
import com.tiffin.system.entity.enums.RequestStatus;
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
public interface TiffinRequestRepository extends JpaRepository<TiffinRequest, Long> {
    
    List<TiffinRequest> findByUserIdOrderByServiceDateDesc(Long userId);
    
    Page<TiffinRequest> findByUserId(Long userId, Pageable pageable);
    
    List<TiffinRequest> findByServiceDate(LocalDate serviceDate);
    
    List<TiffinRequest> findByStatus(RequestStatus status);
    
    @Query("SELECT r FROM TiffinRequest r WHERE r.user.id = :userId AND r.serviceDate = :serviceDate AND r.status NOT IN (:excludedStatuses)")
    List<TiffinRequest> findExistingActiveRequests(
        @Param("userId") Long userId, 
        @Param("serviceDate") LocalDate serviceDate, 
        @Param("excludedStatuses") List<RequestStatus> excludedStatuses
    );

    @Query("SELECT r FROM TiffinRequest r WHERE (:userId IS NULL OR r.user.id = :userId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:startDate IS NULL OR r.serviceDate >= :startDate) " +
           "AND (:endDate IS NULL OR r.serviceDate <= :endDate) " +
           "ORDER BY r.serviceDate DESC, r.createdAt DESC")
    Page<TiffinRequest> searchRequests(
        @Param("userId") Long userId,
        @Param("status") RequestStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    long countByStatus(RequestStatus status);
    long countByServiceDate(LocalDate serviceDate);
}
