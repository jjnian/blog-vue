package com.jinian.blog.service;

import com.jinian.blog.dto.response.CategoryResponse;
import com.jinian.blog.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 获取分类详情
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 创建分类
     */
    CategoryResponse createCategory(Category category);

    /**
     * 更新分类
     */
    CategoryResponse updateCategory(Long id, Category category);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}