package com.blog.back.service.impl;

import com.blog.back.dto.tag.TagRequest;
import com.blog.back.dto.tag.TagResponse;
import com.blog.back.entity.Tag;
import com.blog.back.exception.BusinessException;
import com.blog.back.mapper.TagMapper;
import com.blog.back.service.TagService;
import com.blog.back.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagResponse createTag(TagRequest request) {
        if (tagMapper.existsByName(request.getName())) {
            throw BusinessException.conflict("标签名已存在");
        }
        String slug = resolveSlug(request.getSlug(), request.getName(), null);

        Tag tag = Tag.builder()
                .name(request.getName())
                .slug(slug)
                .color(request.getColor() != null ? request.getColor() : "#6366f1")
                .description(request.getDescription())
                .build();

        tagMapper.insert(tag);  // id 回填
        return toResponse(tag);
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, TagRequest request) {
        Tag tag = getTag(id);

        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            if (tagMapper.existsByName(request.getName())) {
                throw BusinessException.conflict("标签名已存在");
            }
            tag.setName(request.getName());
        }
        if (request.getSlug() != null) {
            tag.setSlug(resolveSlug(request.getSlug(), request.getName(), tag.getSlug()));
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            tag.setDescription(request.getDescription());
        }

        tagMapper.updateById(tag);
        return toResponse(tag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        getTag(id);  // 确认存在
        tagMapper.deleteById(id);
    }

    @Override
    public TagResponse getTagById(Long id) {
        return toResponse(getTag(id));
    }

    @Override
    public TagResponse getTagBySlug(String slug) {
        Tag tag = tagMapper.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));
        return toResponse(tag);
    }

    @Override
    public List<TagResponse> getAllTags() {
        return tagMapper.findAllOrderByPostCountDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ---- 私有方法 ----

    private Tag getTag(Long id) {
        return tagMapper.findById(id)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));
    }

    private String resolveSlug(String requestSlug, String name, String existingSlug) {
        String base = (requestSlug != null && !requestSlug.isBlank())
                ? SlugUtil.toSlug(requestSlug)
                : SlugUtil.toSlug(name);

        if (base.equals(existingSlug)) {
            return existingSlug;
        }

        String slug = base;
        int i = 1;
        while (tagMapper.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .color(tag.getColor())
                .description(tag.getDescription())
                .postCount(tag.getPostCount() != null ? tag.getPostCount() : 0)
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
