package com.blog.back.dto.post;

import com.blog.back.dto.tag.TagResponse;
import com.blog.back.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;       // 完整 Markdown 内容（详情接口返回）
    private String contentHtml;   // 渲染后的 HTML（详情接口返回）
    private String coverImage;
    private PostStatus status;
    private Boolean pinned;
    private Long viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 作者信息
    private Long authorId;
    private String authorName;
    private String authorAvatar;

    // 标签列表
    private List<TagResponse> tags;
}
