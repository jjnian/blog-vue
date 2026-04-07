package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体
 */
@Data
@TableName("roles")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    /**
     * 角色权限列表 (非数据库字段)
     */
    @TableField(exist = false)
    private List<Permission> permissions;

    /**
     * 角色菜单列表 (非数据库字段)
     */
    @TableField(exist = false)
    private List<Menu> menus;
}