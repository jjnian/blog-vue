package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.dto.request.WishRequest;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.WishResponse;
import com.jinian.blog.entity.Wish;
import com.jinian.blog.mapper.WishMapper;
import com.jinian.blog.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 许愿树服务实现
 */
@Service
@RequiredArgsConstructor
public class WishServiceImpl implements WishService {

    private final WishMapper wishMapper;

    @Override
    public PageResponse<WishResponse> getWishes(int page, int size) {
        Page<Wish> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Wish::getDeleted, 0)
               .eq(Wish::getStatus, "ACTIVE")
               .orderByDesc(Wish::getCreatedAt);

        Page<Wish> wishPage = wishMapper.selectPage(pageParam, wrapper);

        List<WishResponse> records = wishPage.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<WishResponse>builder()
                .records(records)
                .total(wishPage.getTotal())
                .pages(wishPage.getPages())
                .current(wishPage.getCurrent())
                .size(wishPage.getSize())
                .build();
    }

    @Override
    public WishResponse createWish(WishRequest request, Long userId, String ipAddress) {
        Wish wish = new Wish();
        wish.setContent(request.getContent());
        wish.setUserId(userId);
        wish.setGuestName(request.getGuestName());
        wish.setColor(request.getColor());
        wish.setPositionX(request.getPositionX());
        wish.setPositionY(request.getPositionY());
        wish.setIpAddress(ipAddress);
        wish.setStatus("ACTIVE");
        wish.setCreatedAt(LocalDateTime.now());

        wishMapper.insert(wish);
        return toResponse(wish);
    }

    @Override
    public void deleteWish(Long id) {
        wishMapper.deleteById(id);
    }

    private WishResponse toResponse(Wish wish) {
        return WishResponse.builder()
                .id(wish.getId())
                .content(wish.getContent())
                .guestName(wish.getGuestName())
                .guestAvatar(wish.getGuestAvatar())
                .color(wish.getColor())
                .positionX(wish.getPositionX())
                .positionY(wish.getPositionY())
                .createdAt(wish.getCreatedAt())
                .build();
    }
}