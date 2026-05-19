package com.blog.back.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名最多 50 个字符")
    private String name;

    @Size(max = 50, message = "slug 最多 50 个字符")
    private String slug;

    @Size(max = 10, message = "颜色值最多 10 个字符")
    private String color;

    @Size(max = 200, message = "描述最多 200 个字符")
    private String description;
}
