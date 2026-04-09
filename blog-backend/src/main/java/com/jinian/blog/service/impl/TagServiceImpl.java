package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.TagResponse;
import com.jinian.blog.entity.TagEntity;
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
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getDeleted, 0)
               .orderByAsc(TagEntity::getName);

        return tagMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponse getTagById(Long id) {
        TagEntity tagEntity = tagMapper.selectById(id);
        if (tagEntity == null || tagEntity.getDeleted() == 1) {
            throw new ResourceNotFoundException("标签", id);
        }
        return toResponse(tagEntity);
    }

    @Override
    public TagResponse createTag(TagEntity tagEntity) {
        tagEntity.setCreatedAt(LocalDateTime.now());
        tagEntity.setArticleCount(0);
        tagMapper.insert(tagEntity);
        return toResponse(tagEntity);
    }

    @Override
    public TagResponse updateTag(Long id, TagEntity tagEntity) {
        TagEntity existing = tagMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new ResourceNotFoundException("标签", id);
        }
        tagEntity.setId(id);
        tagMapper.updateById(tagEntity);
        return toResponse(tagMapper.selectById(id));
    }

    @Override
    public void deleteTag(Long id) {
        TagEntity tagEntity = tagMapper.selectById(id);
        if (tagEntity == null) {
            throw new ResourceNotFoundException("标签", id);
        }
        tagMapper.deleteById(id);
    }

    private TagResponse toResponse(TagEntity tagEntity) {
        return TagResponse.builder()
                .id(tagEntity.getId())
                .name(tagEntity.getName())
                .slug(tagEntity.getSlug())
                .color(tagEntity.getColor())
                .articleCount(tagEntity.getArticleCount())
                .createdAt(tagEntity.getCreatedAt())
                .build();
    }
}