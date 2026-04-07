package com.jinian.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建文章请求
 */
@Data
public class ArticleCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String slug;

    private String excerpt;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImage;

    private String status;

    private Boolean isTop;

    private Boolean allowComment;

    private Long categoryId;

    private List<Long> tagIds;
}