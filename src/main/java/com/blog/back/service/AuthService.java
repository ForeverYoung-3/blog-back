package com.blog.back.service;

import com.blog.back.dto.auth.AuthResponse;
import com.blog.back.dto.auth.LoginRequest;
import com.blog.back.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
