package com.tiffin.system.service.impl;

import com.tiffin.system.dto.*;
import com.tiffin.system.entity.User;
import com.tiffin.system.entity.enums.RoleType;
import com.tiffin.system.entity.enums.UserStatus;
import com.tiffin.system.exception.BadRequestException;
import com.tiffin.system.exception.ResourceNotFoundException;
import com.tiffin.system.repository.UserRepository;
import com.tiffin.system.security.UserDetailsImpl;
import com.tiffin.system.security.jwt.JwtUtils;
import com.tiffin.system.service.AuditService;
import com.tiffin.system.service.AuthService;
import com.tiffin.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final AuditService auditService;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail().trim().toLowerCase(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Your account is currently inactive. Please contact the administrator.");
        }

        BigDecimal balance = userService.getUserOutstandingBalance(user.getId());

        auditService.logAction("LOGIN", "User", user.getId().toString(), user.getEmail(), "User logged in successfully");

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .outstandingBalance(balance)
                .build();
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest registerRequest, String creatorEmail) {
        String email = registerRequest.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with email '" + email + "' already exists");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName().trim())
                .phone(registerRequest.getPhone())
                .department(registerRequest.getDepartment())
                .role(registerRequest.getRole() != null ? registerRequest.getRole() : RoleType.ROLE_EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        auditService.logAction("CREATE_USER", "User", savedUser.getId().toString(),
                creatorEmail != null ? creatorEmail : savedUser.getEmail(),
                "Registered new user " + savedUser.getFullName() + " (" + savedUser.getRole() + ")");

        return userService.mapToDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile(String email) {
        return userService.getUserByEmail(email);
    }
}
