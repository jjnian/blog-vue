package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体
 */
@Data
@TableName("articles")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String slug;

    private String excerpt;

    private String content;

    private String coverImage;

    private Integer views;

    private Integer likes;

    private Integer comments;

    private String status;

    private Boolean isTop;

    private Boolean allowComment;

    private Long authorId;

    private Long categoryId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    @TableLogic
    private Integer deleted;

    /**
     * 分类 (非数据库字段)
     */
    @TableField(exist = false)
    private Category category;

    /**
     * 标签列表 (非数据库字段)
     */
    @TableField(exist = false)
    private List<Tag> tags;

    /**
     * 作者信息 (非数据库字段)
     */
    @TableField(exist = false)
    private User author;
}