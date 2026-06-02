package com.blog.back.dto.user;

import com.blog.back.enums.Role;
import com.blog.back.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String bio;
    private Role role;
    private Boolean enabled;
    private UserStatus status;
    private Long postCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
