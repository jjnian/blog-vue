package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签响应
 */
@Data
@Builder
public class TagResponse {

    private Long id;
    private String name;
    private String slug;
    private String color;
    private Integer articleCount;
    private LocalDateTime createdAt;
}