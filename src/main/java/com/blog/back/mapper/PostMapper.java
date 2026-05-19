package com.blog.back.mapper;

import com.blog.back.entity.Post;
import com.blog.back.enums.PostStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PostMapper {

    /** 插入文章，自动回填 id */
    void insert(Post post);

    /** 按 id 更新（只更新非 null 字段） */
    void updateById(Post post);

    /** 按 id 删除 */
    void deleteById(@Param("id") Long id);

    /** 按 id 查询（含作者信息，不含标签） */
    Optional<Post> findById(@Param("id") Long id);

    /** 按 slug 查询（含作者信息，不含标签） */
    Optional<Post> findBySlug(@Param("slug") String slug);

    /** 查询已发布文章列表（分页由 PageHelper 拦截） */
    List<Post> findByStatus(@Param("status") PostStatus status);

    /** 查询某作者的文章列表 */
    List<Post> findByAuthorId(@Param("authorId") Long authorId);

    /** 查询某作者指定状态的文章列表 */
    List<Post> findByAuthorIdAndStatus(@Param("authorId") Long authorId,
                                       @Param("status") PostStatus status);

    /** 按标签 slug 查询已发布文章 */
    List<Post> findByTagSlugAndStatus(@Param("tagSlug") String tagSlug,
                                      @Param("status") PostStatus status);

    /** 关键词搜索已发布文章（标题/摘要模糊匹配） */
    List<Post> searchByKeyword(@Param("keyword") String keyword,
                               @Param("status") PostStatus status);

    /** 管理员查询所有文章（可按状态过滤，null 表示全部） */
    List<Post> findAllForAdmin(@Param("status") PostStatus status);

    /** slug 是否存在 */
    boolean existsBySlug(@Param("slug") String slug);

    /** 浏览数 +1 */
    void incrementViewCount(@Param("id") Long id);

    /** 统计总文章数 */
    long count();

    /** 按状态统计文章数 */
    long countByStatus(@Param("status") PostStatus status);

    /** 统计某作者的文章数 */
    long countByAuthorId(@Param("authorId") Long authorId);

    /** 统计所有文章的总浏览量 */
    long sumViewCount();
}
