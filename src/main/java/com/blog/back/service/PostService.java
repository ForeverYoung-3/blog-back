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
    PostResponse getPostById(Long id);
    PostResponse getPostBySlug(String slug);
    Page<PostResponse> getPublishedPosts(Pageable pageable);
    Page<PostResponse> getPostsByTag(String tagSlug, Pageable pageable);
    Page<PostResponse> searchPosts(String keyword, Pageable pageable);
    Page<PostResponse> getMyPosts(String username, PostStatus status, Pageable pageable);
    Page<PostResponse> getAllPostsForAdmin(PostStatus status, Pageable pageable);
    PostResponse publishPost(Long id, String username);
    PostResponse archivePost(Long id, String username);
}
