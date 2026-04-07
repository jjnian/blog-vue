package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言墙/弹幕实体
 */
@Data
@TableName("messages")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;

    private Long userId;

    private String guestName;

    private String guestAvatar;

    private String animationType;

    private String color;

    private String fontSize;

    private String ipAddress;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}