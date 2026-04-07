package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 友链实体
 */
@Data
@TableName("links")
public class Link {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String url;

    private String description;

    private String avatar;

    private String status;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}