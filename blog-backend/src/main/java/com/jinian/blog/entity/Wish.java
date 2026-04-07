package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 许愿树实体
 */
@Data
@TableName("wishes")
public class Wish {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long userId;

    private String guestName;

    private String guestAvatar;

    private String color;

    private BigDecimal positionX;

    private BigDecimal positionY;

    private String ipAddress;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}