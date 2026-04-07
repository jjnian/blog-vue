package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.TagResponse;
import com.jinian.blog.entity.Tag;
import com.jinian.blog.mapper.TagMapper;
import com.jinian.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagResponse> getAllTags() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0)
               .orderByAsc(Tag::getName);

        return tagMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new ResourceNotFoundException("标签", id);
        }
        return toResponse(tag);
    }

    @Override
    public TagResponse createTag(Tag tag) {
        tag.setCreatedAt(LocalDateTime.now());
        tag.setArticleCount(0);
        tagMapper.insert(tag);
        return toResponse(tag);
    }

    @Override
    public TagResponse updateTag(Long id, Tag tag) {
        Tag existing = tagMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new ResourceNotFoundException("标签", id);
        }
        tag.setId(id);
        tagMapper.updateById(tag);
        return toResponse(tagMapper.selectById(id));
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new ResourceNotFoundException("标签", id);
        }
        tagMapper.deleteById(id);
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .color(tag.getColor())
                .articleCount(tag.getArticleCount())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}