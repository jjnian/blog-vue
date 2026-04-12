package com.jinian.blog.service;

import com.jinian.blog.entity.Permission;
import com.jinian.blog.entity.UserPermission;

import java.util.List;

/**
 * 权限服务
 */
public interface PermissionService {

    /** 获取用户的有效权限码（角色权限 + 用户覆盖） */
    List<String> getEffectivePermissionCodes(Long userId);

    /** 判断用户是否拥有某权限 */
    boolean hasPermission(Long userId, String permissionCode);

    /** 获取所有功能权限列表 */
    List<Permission> getAllFeaturePermissions();

    /** 获取角色的权限ID列表 */
    List<Long> getRolePermissionIds(Long roleId);

    /** 更新角色的权限 */
    void updateRolePermissions(Long roleId, List<Long> permissionIds);

    /** 获取用户的专属权限覆盖列表 */
    List<UserPermission> getUserPermissionOverrides(Long userId);

    /** 设置用户的单个权限（grant=true授权，false拒绝） */
    void setUserPermission(Long userId, Long permissionId, Boolean granted);

    /** 删除用户的单个权限覆盖（恢复继承角色权限） */
    void deleteUserPermission(Long userId, Long permissionId);
}
