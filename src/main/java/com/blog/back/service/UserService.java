package com.blog.back.service;

import com.blog.back.dto.user.ChangePasswordRequest;
import com.blog.back.dto.user.UpdateUserRequest;
import com.blog.back.dto.user.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    /** 获取当前登录用户信息 */
    UserResponse getCurrentUser(String username);

    /** 当前用户更新自己的资料 */
    UserResponse updateCurrentUser(String username, UpdateUserRequest request);

    /** 当前用户修改密码 */
    void changePassword(String username, ChangePasswordRequest request);

    /** 公开的用户主页信息 */
    UserResponse getUserProfile(Long id);

    // ---- 管理员接口 ----

    /** 查询所有用户 */
    List<UserResponse> getAllUsers();

    /** 按 id 查询用户 */
    UserResponse getUserById(Long id);

    /** 管理员更新用户（含角色/状态） */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /** 删除用户 */
    void deleteUser(Long id);

    /** 统计概览数据（管理员用，含用户数等敏感字段） */
    Map<String, Object> getStats();

    /** 公开统计数据（无需登录：已发布文章数、标签数、总浏览量） */
    Map<String, Object> getPublicStats();
}
