package com.jinian.blog.controller.admin;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.MenuResponse;
import com.jinian.blog.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台菜单管理
 */
@Tag(name = "后台菜单管理", description = "菜单树与角色菜单分配")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    @Operation(summary = "获取全部菜单树")
    @GetMapping("/menus")
    public Result<List<MenuResponse>> getAllMenus() {
        return Result.success(menuService.getAllMenus());
    }

    @Operation(summary = "获取角色已分配菜单ID")
    @GetMapping("/roles/{roleId}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(menuService.getMenuIdsByRoleId(roleId));
    }

    @Operation(summary = "更新角色菜单分配")
    @PutMapping("/roles/{roleId}/menus")
    public Result<Void> updateRoleMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        menuService.updateRoleMenus(roleId, menuIds);
        return Result.success();
    }
}
