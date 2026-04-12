package com.jinian.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户专属权限（覆盖角色默认权限）
 */
@Data
@TableName("user_permissions")
public class UserPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long permissionId;

    /** true=明确授权，false=明确拒绝 */
    private Boolean granted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
