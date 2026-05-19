package com.blog.back.dto.user;

import com.blog.back.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 100, message = "昵称最多 100 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 500, message = "个人简介最多 500 个字符")
    private String bio;

    private String avatar;

    // 仅管理员可修改
    private Role role;
    private Boolean enabled;
}
