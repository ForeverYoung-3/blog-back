package com.blog.back.controller;

import com.blog.back.dto.ApiResponse;
import com.blog.back.dto.post.PostRequest;
import com.blog.back.dto.post.PostResponse;
import com.blog.back.enums.PostStatus;
import com.blog.back.service.PostService;
import com.blog.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserService userService;

    // ---- 公开接口 ----

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("pinned").descending().and(Sort.by("publishedAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(postService.getPublishedPosts(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.success(postService.getPostById(id, username)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(ApiResponse.success(postService.getPostBySlug(slug, username)));
    }

    @GetMapping("/tag/{tagSlug}")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByTag(
            @PathVariable String tagSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(postService.getPostsByTag(tagSlug, pageable)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getPublicStats() {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicStats()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(postService.searchPosts(keyword, pageable)));
    }

    // ---- 需要认证的接口 ----

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EDITOR')")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("文章创建成功", postService.createPost(request, userDetails.getUsername())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("文章更新成功", postService.updatePost(id, request, userDetails.getUsername())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("文章删除成功", null));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> publishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("文章已发布", postService.publishPost(id, userDetails.getUsername())));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> archivePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("文章已归档", postService.archivePost(id, userDetails.getUsername())));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getMyPosts(
            @RequestParam(required = false) PostStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(postService.getMyPosts(userDetails.getUsername(), status, pageable)));
    }
}
