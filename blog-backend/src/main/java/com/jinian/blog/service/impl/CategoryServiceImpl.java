package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.CategoryResponse;
import com.jinian.blog.entity.Category;
import com.jinian.blog.mapper.CategoryMapper;
import com.jinian.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getDeleted, 0)
               .orderByAsc(Category::getSortOrder);

        return categoryMapper.selectList(wrapper).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new ResourceNotFoundException("分类", id);
        }
        return toResponse(category);
    }

    @Override
    public CategoryResponse createCategory(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setArticleCount(0);
        categoryMapper.insert(category);
        return toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, Category category) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new ResourceNotFoundException("分类", id);
        }
        category.setId(id);
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        return toResponse(categoryMapper.selectById(id));
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ResourceNotFoundException("分类", id);
        }
        categoryMapper.deleteById(id);
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .sortOrder(category.getSortOrder())
                .articleCount(category.getArticleCount())
                .createdAt(category.getCreatedAt())
                .build();
    }
}