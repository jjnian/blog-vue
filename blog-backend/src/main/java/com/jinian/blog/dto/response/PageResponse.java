package com.jinian.blog.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 分页响应
 */
@Data
@Builder
public class PageResponse<T> {

    private List<T> records;
    private Long total;
    private Long pages;
    private Long current;
    private Long size;
}