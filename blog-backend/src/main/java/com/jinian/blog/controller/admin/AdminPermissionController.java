package com.jinian.blog.controller.admin;

import com.jinian.blog.common.Result;
import com.jinian.blog.entity.Permission;
import com.jinian.blog.entity.UserPermission;
import com.jinian.blog.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 权限管理
 */
@Tag(name = "管理后台-权限", description = "功能权限管理接口")
@RestController
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
public class AdminPermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取所有功能权限")
    @GetMapping
    public Result<List<Permission>> getAllPermissions() {
        return Result.success(permissionService.getAllFeaturePermissions());
    }

    // ─────────── 角色权限 ───────────

    @Operation(summary = "获取角色的权限ID列表")
    @GetMapping("/roles/{roleId}")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return Result.success(permissionService.getRolePermissionIds(roleId));
    }

    @Operation(summary = "更新角色的权限")
    @PutMapping("/roles/{roleId}")
    public Result<Void> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        permissionService.updateRolePermissions(roleId, permissionIds);
        return Result.success();
    }

    // ─────────── 用户专属权限 ───────────

    @Operation(summary = "获取用户的专属权限覆盖")
    @GetMapping("/users/{userId}")
    public Result<List<UserPermission>> getUserPermissions(@PathVariable Long userId) {
        return Result.success(permissionService.getUserPermissionOverrides(userId));
    }

    @Operation(summary = "设置用户的单个权限覆盖")
    @PutMapping("/users/{userId}/{permissionId}")
    public Result<Void> setUserPermission(
            @PathVariable Long userId,
            @PathVariable Long permissionId,
            @RequestBody SetPermissionRequest req) {
        permissionService.setUserPermission(userId, permissionId, req.getGranted());
        return Result.success();
    }

    @Operation(summary = "删除用户的单个权限覆盖（恢复继承角色权限）")
    @DeleteMapping("/users/{userId}/{permissionId}")
    public Result<Void> deleteUserPermission(
            @PathVariable Long userId,
            @PathVariable Long permissionId) {
        permissionService.deleteUserPermission(userId, permissionId);
        return Result.success();
    }

    @Data
    public static class SetPermissionRequest {
        private Boolean granted;
    }
}
