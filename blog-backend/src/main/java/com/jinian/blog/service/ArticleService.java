package com.jinian.blog.service;

import com.jinian.blog.dto.request.ArticleCreateRequest;
import com.jinian.blog.dto.response.ArticleResponse;
import com.jinian.blog.dto.response.CategoryResponse;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.TagResponse;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 分页获取文章列表
     */
    PageResponse<ArticleResponse> getArticlePage(int page, int size, String status, Long categoryId, Long tagId);

    /**
     * 获取文章详情
     */
    ArticleResponse getArticleById(Long id);

    /**
     * 通过slug获取文章
     */
    ArticleResponse getArticleBySlug(String slug);

    /**
     * 创建文章
     */
    ArticleResponse createArticle(ArticleCreateRequest request, Long authorId);

    /**
     * 更新文章
     */
    ArticleResponse updateArticle(Long id, ArticleCreateRequest request);

    /**
     * 删除文章
     */
    void deleteArticle(Long id);

    /**
     * 增加浏览量
     */
    void incrementViews(Long id);

    /**
     * 获取归档列表
     */
    List<Object[]> getArchives();
}