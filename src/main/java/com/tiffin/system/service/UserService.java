package com.tiffin.system.service;

import com.tiffin.system.dto.UserDto;
import com.tiffin.system.dto.UserStatusUpdateRequest;
import com.tiffin.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface UserService {
    List<UserDto> getAllEmployees();
    Page<UserDto> searchUsers(String query, Pageable pageable);
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    User getUserEntityById(Long id);
    UserDto updateUserStatus(Long id, UserStatusUpdateRequest request, String adminEmail);
    void resetUserPassword(Long id, String newPassword, String adminEmail);
    BigDecimal getUserOutstandingBalance(Long userId);
    UserDto mapToDto(User user);
}