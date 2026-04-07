package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链响应
 */
@Data
@Builder
public class LinkResponse {

    private Long id;
    private String name;
    private String url;
    private String description;
    private String avatar;
    private String status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}