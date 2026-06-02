package com.blog.back.service.impl;

import com.blog.back.dto.auth.AuthResponse;
import com.blog.back.dto.auth.LoginRequest;
import com.blog.back.dto.auth.RegisterRequest;
import com.blog.back.entity.User;
import com.blog.back.enums.Role;
import com.blog.back.enums.UserStatus;
import com.blog.back.exception.BusinessException;
import com.blog.back.mapper.UserMapper;
import com.blog.back.security.JwtUtil;
import com.blog.back.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        String token = jwtUtil.generateToken(user.getUsername());
        return buildAuthResponse(user, token);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.existsByUsername(request.getUsername())) {
            throw BusinessException.conflict("用户名已存在");
        }
        if (userMapper.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("邮箱已被注册");
        }

        // 第一个注册的用户自动成为管理员，直接激活；其他用户默认待审核
        boolean isFirstUser = userMapper.count() == 0;
        Role role = isFirstUser ? Role.ROLE_ADMIN : Role.ROLE_VIEWER;
        UserStatus status = isFirstUser ? UserStatus.ACTIVE : UserStatus.PENDING;

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(role)
                .enabled(true)
                .status(status)
                .build();

        userMapper.insert(user);  // insert 后 user.id 已被回填

        if (status == UserStatus.PENDING) {
            // 待审核用户不返回 token，前端根据 accessToken 为空判断
            return AuthResponse.builder()
                    .accessToken(null)
                    .tokenType(null)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .role(user.getRole().name())
                    .status(status.name())
                    .build();
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole().name())
                .status(user.getStatus() != null ? user.getStatus().name() : "ACTIVE")
                .build();
    }
}
