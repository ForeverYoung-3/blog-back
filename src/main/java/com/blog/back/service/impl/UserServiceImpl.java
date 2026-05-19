package com.blog.back.service.impl;

import com.blog.back.dto.user.UpdateUserRequest;
import com.blog.back.dto.user.UserResponse;
import com.blog.back.entity.User;
import com.blog.back.enums.PostStatus;
import com.blog.back.exception.BusinessException;
import com.blog.back.mapper.PostMapper;
import com.blog.back.mapper.TagMapper;
import com.blog.back.mapper.UserMapper;
import com.blog.back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(String username, UpdateUserRequest request) {
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if(request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio()      != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar()   != null) {
            user.setAvatar(request.getAvatar());
        }
        // 普通用户不能修改 role 和 enabled

        userMapper.updateById(user);
        return toResponse(user);
    }

    @Override
    public UserResponse getUserProfile(Long id) {
        User user = userMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userMapper.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio()      != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar()   != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getRole()     != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled()  != null) {
            user.setEnabled(request.getEnabled());
        }

        userMapper.updateById(user);
        return toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        userMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
        userMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers",     userMapper.count());
        stats.put("totalPosts",     postMapper.count());
        stats.put("publishedPosts", postMapper.countByStatus(PostStatus.PUBLISHED));
        stats.put("draftPosts",     postMapper.countByStatus(PostStatus.DRAFT));
        stats.put("totalTags",      tagMapper.count());
        stats.put("totalViews",     postMapper.sumViewCount());
        return stats;
    }

    @Override
    public Map<String, Object> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", postMapper.countByStatus(PostStatus.PUBLISHED));
        stats.put("totalTags",  tagMapper.count());
        stats.put("totalViews", postMapper.sumViewCount());
        return stats;
    }

    // ---- 私有方法 ----

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .postCount(postMapper.countByAuthorId(user.getId()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
