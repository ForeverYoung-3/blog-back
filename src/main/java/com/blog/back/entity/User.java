package com.blog.back.entity;

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
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String nickname;
    private String avatar;
    private String bio;
    private Role role;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
