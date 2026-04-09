package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.Category;
import com.jinian.blog.mapper.CategoryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 分类管理
 */
@Tag(name = "管理后台-分类", description = "分类管理接口")
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryMapper categoryMapper;

    @Operation(summary = "获取分类列表（分页）")
    @GetMapping
    public Result<PageResponse<Category>> getCategories(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Page<Category> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Category::getName, keyword);
        }

        wrapper.orderByAsc(Category::getSortOrder).orderByDesc(Category::getCreatedAt);

        Page<Category> categoryPage = categoryMapper.selectPage(pageParam, wrapper);

        return Result.success(PageResponse.<Category>builder()
                .records(categoryPage.getRecords())
                .total(categoryPage.getTotal())
                .pages(categoryPage.getPages())
                .current(categoryPage.getCurrent())
                .size(categoryPage.getSize())
                .build());
    }

    @Operation(summary = "获取所有分类（不分页）")
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0).orderByAsc(Category::getSortOrder);
        return Result.success(categoryMapper.selectList(wrapper));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        category.setCreatedAt(LocalDateTime.now());
        if (category.getArticleCount() == null) {
            category.setArticleCount(0);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        return Result.success(category);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "分类不存在");
        }
        category.setId(id);
        categoryMapper.updateById(category);
        return Result.success(categoryMapper.selectById(id));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return Result.error(404, "分类不存在");
        }
        categoryMapper.deleteById(id);
        return Result.success();
    }
}
