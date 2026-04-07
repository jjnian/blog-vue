package com.jinian.blog.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 文章状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum ArticleStatus {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    ARCHIVED("ARCHIVED", "已归档");

    @EnumValue
    private final String code;
    @JsonValue
    private final String desc;
}