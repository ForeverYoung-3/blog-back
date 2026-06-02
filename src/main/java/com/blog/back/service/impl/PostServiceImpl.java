package com.blog.back.service.impl;

import com.blog.back.dto.post.PostRequest;
import com.blog.back.dto.post.PostResponse;
import com.blog.back.dto.tag.TagResponse;
import com.blog.back.entity.Post;
import com.blog.back.entity.Tag;
import com.blog.back.entity.User;
import com.blog.back.enums.PostStatus;
import com.blog.back.enums.Role;
import com.blog.back.exception.BusinessException;
import com.blog.back.mapper.PostMapper;
import com.blog.back.mapper.TagMapper;
import com.blog.back.mapper.UserMapper;
import com.blog.back.service.PostService;
import com.blog.back.util.SlugUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;

    private final Parser markdownParser = Parser.builder()
            .extensions(List.of(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();

    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder()
            .extensions(List.of(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();

    // ==================== 写操作 ====================

    @Override
    @Transactional
    public PostResponse createPost(PostRequest request, String username) {
        User author = getUser(username);
        String slug = resolveSlug(request.getSlug(), request.getTitle(), null);
        List<Long> tagIds = request.getTagIds();

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .summary(request.getSummary())
                .content(request.getContent())
                .coverImage(request.getCoverImage())
                .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
                .pinned(request.getPinned() != null ? request.getPinned() : false)
                .viewCount(0L)
                .authorId(author.getId())
                .build();

        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishedAt(LocalDateTime.now());
        }

        postMapper.insert(post);  // id 回填

        // 保存标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            tagMapper.insertPostTags(post.getId(), tagIds);
        }

        return toResponse(post, author, loadTags(post.getId()), true);
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, String username) {
        Post post = getPost(id);
        User user = getUser(username);
        checkOwnerOrAdmin(post, user);

        if (request.getTitle()      != null) post.setTitle(request.getTitle());
        if (request.getSummary()    != null) post.setSummary(request.getSummary());
        if (request.getContent()    != null) post.setContent(request.getContent());
        if (request.getCoverImage() != null) post.setCoverImage(request.getCoverImage());
        if (request.getPinned()     != null) post.setPinned(request.getPinned());

        if (request.getSlug() != null || request.getTitle() != null) {
            post.setSlug(resolveSlug(request.getSlug(), request.getTitle(), post.getSlug()));
        }

        if (request.getStatus() != null && request.getStatus() != post.getStatus()) {
            post.setStatus(request.getStatus());
            if (request.getStatus() == PostStatus.PUBLISHED && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
        }

        postMapper.updateById(post);

        // 更新标签关联
        if (request.getTagIds() != null) {
            tagMapper.deletePostTagsByPostId(post.getId());
            if (!request.getTagIds().isEmpty()) {
                tagMapper.insertPostTags(post.getId(), request.getTagIds());
            }
        }

        // 重新加载作者和标签
        User author = getUser(username);
        return toResponse(post, author, loadTags(post.getId()), true);
    }

    @Override
    @Transactional
    public void deletePost(Long id, String username) {
        Post post = getPost(id);
        User user = getUser(username);
        checkOwnerOrAdmin(post, user);
        tagMapper.deletePostTagsByPostId(id);
        postMapper.deleteById(id);
    }

    @Override
    @Transactional
    public PostResponse getPostById(Long id, String username) {
        Post post = getPost(id);
        checkHiddenAccess(post, username);
        postMapper.incrementViewCount(id);
        User author = userMapper.findById(post.getAuthorId())
                .orElseThrow(() -> BusinessException.notFound("作者不存在"));
        return toResponse(post, author, loadTags(id), true);
    }

    @Override
    @Transactional
    public PostResponse getPostBySlug(String slug, String username) {
        Post post = postMapper.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
        checkHiddenAccess(post, username);
        postMapper.incrementViewCount(post.getId());
        User author = userMapper.findById(post.getAuthorId())
                .orElseThrow(() -> BusinessException.notFound("作者不存在"));
        return toResponse(post, author, loadTags(post.getId()), true);
    }

    // ==================== 列表查询（PageHelper 分页） ====================

    @Override
    public Page<PostResponse> getPublishedPosts(Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<Post> list = postMapper.findByStatus(PostStatus.PUBLISHED);
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        return toPage(pageInfo, pageable, false);
    }

    @Override
    public Page<PostResponse> getPostsByTag(String tagSlug, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<Post> list = postMapper.findByTagSlugAndStatus(tagSlug, PostStatus.PUBLISHED);
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        return toPage(pageInfo, pageable, false);
    }

    @Override
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<Post> list = postMapper.searchByKeyword(keyword, PostStatus.PUBLISHED);
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        return toPage(pageInfo, pageable, false);
    }

    @Override
    public Page<PostResponse> getMyPosts(String username, PostStatus status, Pageable pageable) {
        User user = getUser(username);
        PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<Post> list = (status != null)
                ? postMapper.findByAuthorIdAndStatus(user.getId(), status)
                : postMapper.findByAuthorId(user.getId());
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        return toPage(pageInfo, pageable, false);
    }

    @Override
    public Page<PostResponse> getAllPostsForAdmin(PostStatus status, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        List<Post> list = postMapper.findAllForAdmin(status);
        PageInfo<Post> pageInfo = new PageInfo<>(list);
        return toPage(pageInfo, pageable, false);
    }

    // ==================== 状态变更 ====================

    @Override
    @Transactional
    public PostResponse publishPost(Long id, String username) {
        Post post = getPost(id);
        checkOwnerOrAdmin(post, getUser(username));
        post.setStatus(PostStatus.PUBLISHED);
        if (post.getPublishedAt() == null) post.setPublishedAt(LocalDateTime.now());
        postMapper.updateById(post);
        User author = userMapper.findById(post.getAuthorId()).orElseThrow();
        return toResponse(post, author, loadTags(id), true);
    }

    @Override
    @Transactional
    public PostResponse archivePost(Long id, String username) {
        Post post = getPost(id);
        checkOwnerOrAdmin(post, getUser(username));
        post.setStatus(PostStatus.ARCHIVED);
        postMapper.updateById(post);
        User author = userMapper.findById(post.getAuthorId()).orElseThrow();
        return toResponse(post, author, loadTags(id), true);
    }

    // ==================== 私有辅助方法 ====================

    private Post getPost(Long id) {
        return postMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
    }

    private User getUser(String username) {
        return userMapper.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("用户不存在"));
    }

    private void checkOwnerOrAdmin(Post post, User user) {
        if (!post.getAuthorId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw BusinessException.forbidden("无权操作此文章");
        }
    }

    /**
     * HIDDEN 文章仅管理员可见；匿名或普通用户访问时抛 404（不暴露文章存在）
     */
    private void checkHiddenAccess(Post post, String username) {
        if (post.getStatus() != PostStatus.HIDDEN) return;
        if (username == null) throw BusinessException.notFound("文章不存在");
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw BusinessException.notFound("文章不存在");
        }
    }

    private String resolveSlug(String requestSlug, String title, String existingSlug) {
        String base = (requestSlug != null && !requestSlug.isBlank())
                ? SlugUtil.toSlug(requestSlug)
                : SlugUtil.toSlug(title);

        if (base.equals(existingSlug)) return existingSlug;

        String slug = base;
        int i = 1;
        while (postMapper.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    /** 加载文章的标签列表 */
    private List<Tag> loadTags(Long postId) {
        return tagMapper.findByPostId(postId);
    }

    private String renderMarkdown(String markdown) {
        if (markdown == null) return "";
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    /** 将 Post 实体 + 作者 + 标签 组装成 PostResponse */
    private PostResponse toResponse(Post post, User author, List<Tag> tags, boolean includeContent) {
        List<TagResponse> tagResponses = tags.stream()
                .map(t -> TagResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .slug(t.getSlug())
                        .color(t.getColor())
                        .build())
                .collect(Collectors.toList());

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(includeContent ? post.getContent() : null)
                .contentHtml(includeContent ? renderMarkdown(post.getContent()) : null)
                .coverImage(post.getCoverImage())
                .status(post.getStatus())
                .pinned(post.getPinned())
                .viewCount(post.getViewCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .authorId(author.getId())
                .authorName(author.getNickname() != null ? author.getNickname()
                        : (post.getAuthorName() != null ? post.getAuthorName() : author.getUsername()))
                .authorAvatar(author.getAvatar())
                .tags(tagResponses)
                .build();
    }

    /** 列表查询时 Post 已含 authorName/authorAvatar（JOIN 填充），不需要再查 User */
    private PostResponse toListResponse(Post post) {
        List<Tag> tags = loadTags(post.getId());
        List<TagResponse> tagResponses = tags.stream()
                .map(t -> TagResponse.builder()
                        .id(t.getId()).name(t.getName()).slug(t.getSlug()).color(t.getColor())
                        .build())
                .collect(Collectors.toList());

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .coverImage(post.getCoverImage())
                .status(post.getStatus())
                .pinned(post.getPinned())
                .viewCount(post.getViewCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .authorId(post.getAuthorId())
                .authorName(post.getAuthorName())
                .authorAvatar(post.getAuthorAvatar())
                .tags(tagResponses)
                .build();
    }

    /** PageInfo → Spring Page */
    private Page<PostResponse> toPage(PageInfo<Post> pageInfo, Pageable pageable, boolean includeContent) {
        List<PostResponse> content = pageInfo.getList().stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, pageInfo.getTotal());
    }
}
