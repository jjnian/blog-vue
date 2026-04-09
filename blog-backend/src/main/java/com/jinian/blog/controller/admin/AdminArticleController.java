package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.ArticleResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.entity.Article;
import com.jinian.blog.entity.Category;
import com.jinian.blog.entity.TagEntity;
import com.jinian.blog.mapper.ArticleMapper;
import com.jinian.blog.mapper.CategoryMapper;
import com.jinian.blog.mapper.TagMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Admin Articles", description = "Admin article management APIs")
@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Operation(summary = "Get admin article list (paginated)")
    @GetMapping
    public Result<PageResponse<ArticleResponse>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId) {

        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Article::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Article::getCreatedAt);

        Page<Article> articlePage = articleMapper.selectPage(pageParam, wrapper);
        List<ArticleResponse> records = articlePage.getRecords()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return Result.success(PageResponse.<ArticleResponse>builder()
                .records(records)
                .total(articlePage.getTotal())
                .pages(articlePage.getPages())
                .current(articlePage.getCurrent())
                .size(articlePage.getSize())
                .build());
    }

    @Operation(summary = "Update article status")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return Result.error(404, "Article not found");
        }
        article.setStatus(status);
        articleMapper.updateById(article);
        return Result.success();
    }

    @Operation(summary = "Set article top status")
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id, @RequestParam Boolean isTop) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return Result.error(404, "Article not found");
        }
        article.setIsTop(isTop);
        articleMapper.updateById(article);
        return Result.success();
    }

    private ArticleResponse toResponse(Article article) {
        ArticleResponse.ArticleResponseBuilder builder = ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .excerpt(article.getExcerpt())
                .coverImage(article.getCoverImage())
                .views(article.getViews())
                .likes(article.getLikes())
                .comments(article.getComments())
                .status(article.getStatus())
                .isTop(article.getIsTop())
                .createdAt(article.getCreatedAt())
                .publishedAt(article.getPublishedAt());

        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                builder.category(com.jinian.blog.dto.response.CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .color(category.getColor())
                        .build());
            }
        }

        List<TagEntity> tagEntities = tagMapper.selectByArticleId(article.getId());
        builder.tags(tagEntities.stream()
                .map(tag -> com.jinian.blog.dto.response.TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .slug(tag.getSlug())
                        .color(tag.getColor())
                        .build())
                .collect(Collectors.toList()));

        return builder.build();
    }
}
