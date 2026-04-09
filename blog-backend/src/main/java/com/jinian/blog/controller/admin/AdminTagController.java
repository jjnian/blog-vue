package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.TagEntity;
import com.jinian.blog.mapper.TagMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 标签管理
 */
@Tag(name = "管理后台-标签", description = "标签管理接口")
@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagMapper tagMapper;

    @Operation(summary = "获取标签列表（分页）")
    @GetMapping
    public Result<PageResponse<TagEntity>> getTags(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Page<TagEntity> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(TagEntity::getName, keyword);
        }

        wrapper.orderByDesc(TagEntity::getCreatedAt);

        Page<TagEntity> tagPage = tagMapper.selectPage(pageParam, wrapper);

        return Result.success(PageResponse.<TagEntity>builder()
                .records(tagPage.getRecords())
                .total(tagPage.getTotal())
                .pages(tagPage.getPages())
                .current(tagPage.getCurrent())
                .size(tagPage.getSize())
                .build());
    }

    @Operation(summary = "获取所有标签（不分页）")
    @GetMapping("/all")
    public Result<List<TagEntity>> getAllTags() {
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagEntity::getDeleted, 0).orderByDesc(TagEntity::getCreatedAt);
        return Result.success(tagMapper.selectList(wrapper));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagEntity> createTag(@RequestBody TagEntity tag) {
        tag.setCreatedAt(LocalDateTime.now());
        if (tag.getArticleCount() == null) {
            tag.setArticleCount(0);
        }
        tagMapper.insert(tag);
        return Result.success(tag);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagEntity> updateTag(@PathVariable Long id, @RequestBody TagEntity tag) {
        TagEntity existing = tagMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "标签不存在");
        }
        tag.setId(id);
        tagMapper.updateById(tag);
        return Result.success(tagMapper.selectById(id));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        TagEntity tag = tagMapper.selectById(id);
        if (tag == null) {
            return Result.error(404, "标签不存在");
        }
        tagMapper.deleteById(id);
        return Result.success();
    }
}
