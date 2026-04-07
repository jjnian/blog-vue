package com.jinian.blog.service;

import com.jinian.blog.dto.response.TagResponse;
import com.jinian.blog.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    /**
     * 获取所有标签
     */
    List<TagResponse> getAllTags();

    /**
     * 获取标签详情
     */
    TagResponse getTagById(Long id);

    /**
     * 创建标签
     */
    TagResponse createTag(Tag tag);

    /**
     * 更新标签
     */
    TagResponse updateTag(Long id, Tag tag);

    /**
     * 删除标签
     */
    void deleteTag(Long id);
}