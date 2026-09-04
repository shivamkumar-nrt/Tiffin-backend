package com.tiffin.system.repository;

import com.tiffin.system.entity.Payment;
import com.tiffin.system.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE (:userId IS NULL OR p.user.id = :userId) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> searchPayments(
        @Param("userId") Long userId,
        @Param("status") PaymentStatus status,
        Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    long countByStatus(PaymentStatus status);
}
