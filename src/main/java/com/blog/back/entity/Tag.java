package com.blog.back.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    private Long id;
    private String name;
    /** URL slug */
    private String slug;

    @Builder.Default
    private String color = "#6366f1";

    private String description;
    private LocalDateTime createdAt;

    /** 关联文章数（查询时聚合填充，非数据库字段） */
    private Integer postCount;
}
