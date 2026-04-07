package com.jinian.blog.service;

import com.jinian.blog.dto.request.WishRequest;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.WishResponse;

/**
 * 许愿树服务接口
 */
public interface WishService {

    PageResponse<WishResponse> getWishes(int page, int size);

    WishResponse createWish(WishRequest request, Long userId, String ipAddress);

    void deleteWish(Long id);
}