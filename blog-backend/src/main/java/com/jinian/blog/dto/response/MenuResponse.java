package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 菜单响应
 */
@Data
@Builder
public class MenuResponse {

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String path;
    private String icon;
    private String component;
    private Integer sortOrder;
    private Boolean visible;
    private List<MenuResponse> children;
}