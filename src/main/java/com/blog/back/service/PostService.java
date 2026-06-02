package com.blog.back.service;

import com.blog.back.dto.post.PostRequest;
import com.blog.back.dto.post.PostResponse;
import com.blog.back.enums.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostResponse createPost(PostRequest request, String username);
    PostResponse updatePost(Long id, PostRequest request, String username);
    void deletePost(Long id, String username);
    /** username 为 null 表示匿名访问；HIDDEN 文章仅管理员可读 */
    PostResponse getPostById(Long id, String username);
    PostResponse getPostBySlug(String slug, String username);
    Page<PostResponse> getPublishedPosts(Pageable pageable);
    Page<PostResponse> getPostsByTag(String tagSlug, Pageable pageable);
    Page<PostResponse> searchPosts(String keyword, Pageable pageable);
    Page<PostResponse> getMyPosts(String username, PostStatus status, Pageable pageable);
    Page<PostResponse> getAllPostsForAdmin(PostStatus status, Pageable pageable);
    PostResponse publishPost(Long id, String username);
    PostResponse archivePost(Long id, String username);
}
