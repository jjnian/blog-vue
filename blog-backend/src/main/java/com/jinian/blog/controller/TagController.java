package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.TagResponse;
import com.jinian.blog.entity.TagEntity;
import com.jinian.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@Tag(name = "标签", description = "标签相关接口")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "获取所有标签")
    @GetMapping
    public Result<List<TagResponse>> getAllTags() {
        return Result.success(tagService.getAllTags());
    }

    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<TagResponse> getTag(@PathVariable Long id) {
        return Result.success(tagService.getTagById(id));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagResponse> createTag(@RequestBody TagEntity tagEntity) {
        return Result.success(tagService.createTag(tagEntity));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagResponse> updateTag(@PathVariable Long id, @RequestBody TagEntity tagEntity) {
        return Result.success(tagService.updateTag(id, tagEntity));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }
}