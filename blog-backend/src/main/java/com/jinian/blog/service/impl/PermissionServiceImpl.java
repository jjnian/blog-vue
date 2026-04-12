package com.jinian.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.entity.Permission;
import com.jinian.blog.entity.UserPermission;
import com.jinian.blog.mapper.PermissionMapper;
import com.jinian.blog.mapper.UserPermissionMapper;
import com.jinian.blog.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 *
 * 有效权限优先级：
 *   1. 用户专属明确拒绝（granted=false） → 无权限
 *   2. 用户专属明确授权（granted=true）  → 有权限
 *   3. 角色权限继承                       → 有/无权限
 */
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final UserPermissionMapper userPermissionMapper;

    @Override
    public List<String> getEffectivePermissionCodes(Long userId) {
        // 1. 获取角色权限
        List<Permission> rolePerms = permissionMapper.selectByUserId(userId);
        Set<Long> rolePermIds = rolePerms.stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());

        // 2. 获取用户专属覆盖
        List<UserPermission> userOverrides = userPermissionMapper.selectByUserId(userId);
        Map<Long, Boolean> overrideMap = userOverrides.stream()
                .collect(Collectors.toMap(UserPermission::getPermissionId, UserPermission::getGranted));

        // 3. 合并：明确授权的permId集合
        Set<Long> grantedIds = rolePermIds.stream()
                .filter(id -> !Boolean.FALSE.equals(overrideMap.get(id))) // 未被明确拒绝
                .collect(Collectors.toSet());

        // 加入用户专属授权（不在角色中但被单独grant的）
        overrideMap.forEach((permId, granted) -> {
            if (Boolean.TRUE.equals(granted)) grantedIds.add(permId);
        });

        if (grantedIds.isEmpty()) return List.of();

        // 4. 查出这些permission的code
        return permissionMapper.selectBatchIds(grantedIds).stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        return getEffectivePermissionCodes(userId).contains(permissionCode);
    }

    @Override
    public List<Permission> getAllFeaturePermissions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getResourceType, "FEATURE")
                        .orderByAsc(Permission::getId)
        );
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return permissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        permissionMapper.deleteRolePermissions(roleId);
        if (permissionIds != null) {
            for (Long permissionId : permissionIds) {
                permissionMapper.insertRolePermission(roleId, permissionId);
            }
        }
    }

    @Override
    public List<UserPermission> getUserPermissionOverrides(Long userId) {
        return userPermissionMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void setUserPermission(Long userId, Long permissionId, Boolean granted) {
        // 先删除旧的覆盖（如果存在）
        userPermissionMapper.deleteByUserAndPermission(userId, permissionId);

        UserPermission up = new UserPermission();
        up.setUserId(userId);
        up.setPermissionId(permissionId);
        up.setGranted(granted);
        up.setCreatedAt(LocalDateTime.now());
        userPermissionMapper.insert(up);
    }

    @Override
    public void deleteUserPermission(Long userId, Long permissionId) {
        userPermissionMapper.deleteByUserAndPermission(userId, permissionId);
    }
}
