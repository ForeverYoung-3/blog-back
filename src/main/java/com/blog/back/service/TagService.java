package com.blog.back.service;

import com.blog.back.dto.tag.TagRequest;
import com.blog.back.dto.tag.TagResponse;

import java.util.List;

public interface TagService {
    TagResponse createTag(TagRequest request);
    TagResponse updateTag(Long id, TagRequest request);
    void deleteTag(Long id);
    TagResponse getTagById(Long id);
    TagResponse getTagBySlug(String slug);
    List<TagResponse> getAllTags();
}
