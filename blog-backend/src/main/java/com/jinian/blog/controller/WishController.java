package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.request.WishRequest;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.WishResponse;
import com.jinian.blog.service.WishService;
import com.jinian.blog.util.SecurityUtils;
import com.jinian.blog.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 许愿树控制器
 */
@Tag(name = "许愿树", description = "许愿树相关接口")
@RestController
@RequestMapping("/wishes")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @Operation(summary = "获取许愿列表")
    @GetMapping
    public Result<PageResponse<WishResponse>> getWishes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(wishService.getWishes(page, size));
    }

    @Operation(summary = "发送许愿")
    @PostMapping
    public Result<WishResponse> createWish(@Valid @RequestBody WishRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String ipAddress = WebUtils.getClientIp();
        return Result.success(wishService.createWish(request, userId, ipAddress));
    }

    @Operation(summary = "删除许愿")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWish(@PathVariable Long id) {
        wishService.deleteWish(id);
        return Result.success();
    }
}