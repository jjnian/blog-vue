package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.request.ArticleCreateRequest;
import com.jinian.blog.dto.response.ArticleResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.service.ArticleService;
import com.jinian.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@Tag(name = "文章", description = "文章相关接口")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "获取文章列表")
    @GetMapping
    public Result<PageResponse<ArticleResponse>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        return Result.success(articleService.getArticlePage(page, size, status, categoryId, tagId));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticle(@PathVariable Long id) {
        ArticleResponse article = articleService.getArticleById(id);
        articleService.incrementViews(id);
        return Result.success(article);
    }

    @Operation(summary = "通过slug获取文章")
    @GetMapping("/slug/{slug}")
    public Result<ArticleResponse> getArticleBySlug(@PathVariable String slug) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        articleService.incrementViews(article.getId());
        return Result.success(article);
    }

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<ArticleResponse> createArticle(@Valid @RequestBody ArticleCreateRequest request) {
        Long authorId = SecurityUtils.getCurrentUserId();
        return Result.success(articleService.createArticle(request, authorId));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<ArticleResponse> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleCreateRequest request) {
        return Result.success(articleService.updateArticle(id, request));
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "获取归档列表")
    @GetMapping("/archives")
    public Result<List<Object[]>> getArchives() {
        return Result.success(articleService.getArchives());
    }
}