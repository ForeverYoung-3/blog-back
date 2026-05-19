package com.blog.back.mapper;

import com.blog.back.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TagMapper {

    /** 插入标签，自动回填 id */
    void insert(Tag tag);

    /** 按 id 更新（只更新非 null 字段） */
    void updateById(Tag tag);

    /** 按 id 删除 */
    void deleteById(@Param("id") Long id);

    /** 按 id 查询 */
    Optional<Tag> findById(@Param("id") Long id);

    /** 按 slug 查询 */
    Optional<Tag> findBySlug(@Param("slug") String slug);

    /** 按 name 查询 */
    Optional<Tag> findByName(@Param("name") String name);

    /** 查询所有标签，按文章数倒序 */
    List<Tag> findAllOrderByPostCountDesc();

    /** 按 id 列表批量查询 */
    List<Tag> findByIds(@Param("ids") List<Long> ids);

    /** 按文章 id 查询关联标签 */
    List<Tag> findByPostId(@Param("postId") Long postId);

    /** name 是否存在 */
    boolean existsByName(@Param("name") String name);

    /** slug 是否存在 */
    boolean existsBySlug(@Param("slug") String slug);

    /** 统计总标签数 */
    long count();

    // ---- post_tags 关联表操作 ----

    /** 批量插入文章-标签关联 */
    void insertPostTags(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);

    /** 删除某篇文章的所有标签关联 */
    void deletePostTagsByPostId(@Param("postId") Long postId);
}
