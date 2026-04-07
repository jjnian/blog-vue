package com.jinian.blog.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户状态枚举
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

    ACTIVE("ACTIVE", "正常"),
    INACTIVE("INACTIVE", "未激活"),
    BANNED("BANNED", "封禁");

    @EnumValue
    private final String code;
    @JsonValue
    private final String desc;
}