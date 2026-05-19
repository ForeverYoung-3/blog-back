package com.blog.back.dto.post;

import com.blog.back.enums.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多 200 个字符")
    private String title;

    @Size(max = 200, message = "slug 最多 200 个字符")
    private String slug;

    @Size(max = 500, message = "摘要最多 500 个字符")
    private String summary;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private PostStatus status;

    private Boolean pinned;

    private List<Long> tagIds;
}
