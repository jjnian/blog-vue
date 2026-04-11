package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.request.ArticleCreateRequest;
import com.jinian.blog.dto.response.ArticleResponse;
import com.jinian.blog.dto.response.CategoryResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.TagResponse;
import com.jinian.blog.entity.Article;
import com.jinian.blog.entity.Category;
import com.jinian.blog.entity.TagEntity;
import com.jinian.blog.mapper.ArticleMapper;
import com.jinian.blog.mapper.CategoryMapper;
import com.jinian.blog.mapper.TagMapper;
import com.jinian.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Override
    public PageResponse<ArticleResponse> getArticlePage(int page, int size, String status, Long categoryId, Long tagId) {
        Page<Article> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Article::getIsTop)
               .orderByDesc(Article::getCreatedAt);

        Page<Article> articlePage = articleMapper.selectPage(pageParam, wrapper);

        List<ArticleResponse> records = articlePage.getRecords().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<ArticleResponse>builder()
                .records(records)
                .total(articlePage.getTotal())
                .pages(articlePage.getPages())
                .current(articlePage.getCurrent())
                .size(articlePage.getSize())
                .build();
    }

    @Override
    public ArticleResponse getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new ResourceNotFoundException("文章", id);
        }
        return toResponse(article);
    }

    @Override
    public ArticleResponse getArticleBySlug(String slug) {
        Article article = articleMapper.selectBySlug(slug);
        if (article == null) {
            throw new ResourceNotFoundException("文章不存在: " + slug);
        }
        return toResponse(article);
    }

    @Override
    @Transactional
    public ArticleResponse createArticle(ArticleCreateRequest request, Long authorId) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setSlug(request.getSlug() != null ? request.getSlug() : generateSlug(request.getTitle()));
        article.setExcerpt(request.getExcerpt());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
        article.setIsTop(request.getIsTop() != null ? request.getIsTop() : false);
        article.setAllowComment(request.getAllowComment() != null ? request.getAllowComment() : true);
        article.setAuthorId(authorId);
        article.setCategoryId(request.getCategoryId());
        article.setViews(0);
        article.setLikes(0);
        article.setComments(0);
        article.setCreatedAt(LocalDateTime.now());

        if ("PUBLISHED".equals(article.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleMapper.insert(article);

        // 保存标签关联
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveArticleTags(article.getId(), request.getTagIds());
        }

        return toResponse(article);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleCreateRequest request) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new ResourceNotFoundException("文章", id);
        }

        article.setTitle(request.getTitle());
        if (request.getSlug() != null) {
            article.setSlug(request.getSlug());
        }
        article.setExcerpt(request.getExcerpt());
        article.setContent(request.getContent());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setIsTop(request.getIsTop());
        article.setAllowComment(request.getAllowComment());
        article.setUpdatedAt(LocalDateTime.now());

        // 状态变更
        if ("PUBLISHED".equals(request.getStatus()) && !"PUBLISHED".equals(article.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setStatus(request.getStatus());

        articleMapper.updateById(article);

        // 更新标签关联
        if (request.getTagIds() != null) {
            articleMapper.deleteArticleTags(id);
            saveArticleTags(id, request.getTagIds());
        }

        return toResponse(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new ResourceNotFoundException("文章", id);
        }
        articleMapper.deleteById(id);
    }

    @Override
    public void incrementViews(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setViews(article.getViews() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    public List<Object[]> getArchives() {
        return articleMapper.selectArchives();
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            articleMapper.insertArticleTag(articleId, tagId);
        }
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }

    private ArticleResponse toResponse(Article article) {
        // 获取分类
        CategoryResponse categoryResponse = null;
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                categoryResponse = CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .color(category.getColor())
                        .build();
            }
        }

        // 获取标签
        List<TagEntity> tagEntities = tagMapper.selectByArticleId(article.getId());
        List<TagResponse> tagResponses = tagEntities.stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .slug(tag.getSlug())
                        .color(tag.getColor())
                        .build())
                .collect(Collectors.toList());

        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .excerpt(article.getExcerpt())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .views(article.getViews())
                .likes(article.getLikes())
                .comments(article.getComments())
                .status(article.getStatus())
                .isTop(article.getIsTop())
                .createdAt(article.getCreatedAt())
                .publishedAt(article.getPublishedAt())
                .category(categoryResponse)
                .tags(tagResponses)
                .build();
    }
}