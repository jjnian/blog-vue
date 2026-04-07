package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.LinkResponse;
import com.jinian.blog.entity.Link;
import com.jinian.blog.mapper.LinkMapper;
import com.jinian.blog.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 友链服务实现
 */
@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkMapper linkMapper;

    @Override
    public List<LinkResponse> getAllLinks() {
        LambdaQueryWrapper<Link> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Link::getDeleted, 0)
               .eq(Link::getStatus, "ACTIVE")
               .orderByAsc(Link::getSortOrder);

        return linkMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LinkResponse getLinkById(Long id) {
        Link link = linkMapper.selectById(id);
        if (link == null || link.getDeleted() == 1) {
            throw new ResourceNotFoundException("友链", id);
        }
        return toResponse(link);
    }

    @Override
    public LinkResponse createLink(Link link) {
        link.setCreatedAt(LocalDateTime.now());
        link.setStatus(link.getStatus() != null ? link.getStatus() : "ACTIVE");
        linkMapper.insert(link);
        return toResponse(link);
    }

    @Override
    public LinkResponse updateLink(Long id, Link link) {
        Link existing = linkMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new ResourceNotFoundException("友链", id);
        }
        link.setId(id);
        link.setUpdatedAt(LocalDateTime.now());
        linkMapper.updateById(link);
        return toResponse(linkMapper.selectById(id));
    }

    @Override
    public void deleteLink(Long id) {
        linkMapper.deleteById(id);
    }

    private LinkResponse toResponse(Link link) {
        return LinkResponse.builder()
                .id(link.getId())
                .name(link.getName())
                .url(link.getUrl())
                .description(link.getDescription())
                .avatar(link.getAvatar())
                .status(link.getStatus())
                .sortOrder(link.getSortOrder())
                .createdAt(link.getCreatedAt())
                .build();
    }
}