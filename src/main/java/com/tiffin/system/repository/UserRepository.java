package com.tiffin.system.repository;

import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(RoleType role);
    List<User> findByStatus(UserStatus status);
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}
