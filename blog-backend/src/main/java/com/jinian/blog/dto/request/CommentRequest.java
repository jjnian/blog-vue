package com.jinian.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评论请求
 */
@Data
public class CommentRequest {

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Long articleId;

    private Long parentId;

    private String guestName;

    private String guestEmail;
}