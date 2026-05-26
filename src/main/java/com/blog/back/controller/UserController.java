package com.blog.back.controller;

import com.blog.back.dto.ApiResponse;
import com.blog.back.dto.user.ChangePasswordRequest;
import com.blog.back.dto.user.UpdateUserRequest;
import com.blog.back.dto.user.UserResponse;
import com.blog.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getCurrentUser(userDetails.getUsername())));
    }

    /** 当前用户更新自己的资料 */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("个人信息更新成功",
                userService.updateCurrentUser(userDetails.getUsername(), request)));
    }

    /** 修改当前用户密码 */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    /** 公开的用户主页 */
    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(id)));
    }
}
