package com.tiffin.system.repository;

import com.tiffin.system.entity.BroadcastNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastNotificationRepository extends JpaRepository<BroadcastNotification, Long> {
    List<BroadcastNotification> findTop20ByOrderByCreatedAtDesc();

    @Query("SELECT b FROM BroadcastNotification b WHERE b.targetAudience = 'ALL' OR b.targetUserId = :userId OR (b.targetAudience = 'WITH_DUES' AND :hasDues = true) ORDER BY b.createdAt DESC")
    List<BroadcastNotification> findForUser(@Param("userId") Long userId, @Param("hasDues") boolean hasDues);
}