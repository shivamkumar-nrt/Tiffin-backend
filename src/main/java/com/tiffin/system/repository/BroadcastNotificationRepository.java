package com.tiffin.system.repository;

import com.tiffin.system.entity.BroadcastNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastNotificationRepository extends JpaRepository<BroadcastNotification, Long> {
    List<BroadcastNotification> findTop20ByOrderByCreatedAtDesc();
    List<BroadcastNotification> findByTargetAudienceOrTargetUserIdOrderByCreatedAtDesc(String audience, Long userId);
}