package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.LinkResponse;
import com.jinian.blog.entity.Link;
import com.jinian.blog.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 友链控制器
 */
@Tag(name = "友链", description = "友情链接相关接口")
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @Operation(summary = "获取友链列表")
    @GetMapping
    public Result<List<LinkResponse>> getAllLinks() {
        return Result.success(linkService.getAllLinks());
    }

    @Operation(summary = "获取友链详情")
    @GetMapping("/{id}")
    public Result<LinkResponse> getLink(@PathVariable Long id) {
        return Result.success(linkService.getLinkById(id));
    }

    @Operation(summary = "创建友链")
    @PostMapping
    public Result<LinkResponse> createLink(@RequestBody Link link) {
        return Result.success(linkService.createLink(link));
    }

    @Operation(summary = "更新友链")
    @PutMapping("/{id}")
    public Result<LinkResponse> updateLink(@PathVariable Long id, @RequestBody Link link) {
        return Result.success(linkService.updateLink(id, link));
    }

    @Operation(summary = "删除友链")
    @DeleteMapping("/{id}")
    public Result<Void> deleteLink(@PathVariable Long id) {
        linkService.deleteLink(id);
        return Result.success();
    }
}