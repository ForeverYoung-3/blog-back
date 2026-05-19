package com.blog.back.entity;

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
public class Post {

    private Long id;
    private String title;
    /** URL 友好别名 */
    private String slug;
    private String summary;
    private String content;
    private String coverImage;

    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @Builder.Default
    private Boolean pinned = false;

    @Builder.Default
    private Long viewCount = 0L;

    private LocalDateTime publishedAt;
    private Long authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- 关联查询填充（非数据库字段） ----
    /** 作者信息（JOIN 查询时填充） */
    private String authorName;
    private String authorAvatar;

    /** 标签列表（二次查询填充） */
    private List<Tag> tags;
}
