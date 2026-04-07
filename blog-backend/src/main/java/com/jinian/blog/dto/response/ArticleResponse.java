package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章响应
 */
@Data
@Builder
public class ArticleResponse {

    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String coverImage;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private String status;
    private Boolean isTop;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private CategoryResponse category;
    private List<TagResponse> tags;
    private AuthorInfo author;

    @Data
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}