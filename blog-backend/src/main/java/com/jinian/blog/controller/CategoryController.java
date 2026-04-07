package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.CategoryResponse;
import com.jinian.blog.entity.Category;
import com.jinian.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类", description = "分类相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<CategoryResponse>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategory(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<CategoryResponse> createCategory(@RequestBody Category category) {
        return Result.success(categoryService.createCategory(category));
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return Result.success(categoryService.updateCategory(id, category));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}