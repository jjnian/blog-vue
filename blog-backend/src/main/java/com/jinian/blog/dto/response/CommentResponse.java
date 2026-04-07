package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论响应
 */
@Data
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private Long articleId;
    private Long parentId;
    private UserInfo user;
    private String status;
    private Integer likes;
    private LocalDateTime createdAt;
    private List<CommentResponse> children;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String avatar;
    }
}