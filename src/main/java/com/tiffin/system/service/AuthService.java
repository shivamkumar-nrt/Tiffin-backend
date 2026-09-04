package com.tiffin.system.service;

import com.tiffin.system.dto.AuthRequest;
import com.tiffin.system.dto.AuthResponse;
import com.tiffin.system.dto.RegisterRequest;
import com.tiffin.system.dto.UserDto;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    UserDto register(RegisterRequest registerRequest, String creatorEmail);
    UserDto getCurrentUserProfile(String email);
}
