package com.tiffin.system.service.impl;

import com.tiffin.system.dto.UserDto;
import com.tiffin.system.dto.UserStatusUpdateRequest;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RecordStatus;
import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.entity.enums.UserStatus;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.TiffinRecordRepository;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TiffinRecordRepository tiffinRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllEmployees() {
        return userRepository.findByRole(RoleType.ROLE_EMPLOYEE).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll(pageable).map(this::mapToDto);
        }
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return mapToDto(getUserEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(Long id, UserStatusUpdateRequest request, String adminEmail) {
        User user = getUserEntityById(id);
        UserStatus oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        User updated = userRepository.save(user);

        auditService.logAction("UPDATE_USER_STATUS", "User", id.toString(), adminEmail,
                "Updated status for " + user.getEmail() + " from " + oldStatus + " to " + request.getStatus());

        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void resetUserPassword(Long id, String newPassword, String adminEmail) {
        User user = getUserEntityById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditService.logAction("RESET_USER_PASSWORD", "User", id.toString(), adminEmail,
                "Admin reset password for user " + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUserOutstandingBalance(Long userId) {
        BigDecimal unpaid = tiffinRecordRepository.sumAmountByUserIdAndStatus(userId, RecordStatus.UNPAID);
        return unpaid != null ? unpaid : BigDecimal.ZERO;
    }

    @Override
    public UserDto mapToDto(User user) {
        BigDecimal balance = getUserOutstandingBalance(user.getId());
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .role(user.getRole())
                .status(user.getStatus())
                .outstandingBalance(balance)
                .createdAt(user.getCreatedAt())
                .build();
    }
}