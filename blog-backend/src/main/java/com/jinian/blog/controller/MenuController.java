package com.jinian.blog.controller;

import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.MenuResponse;
import com.jinian.blog.service.MenuService;
import com.jinian.blog.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@Tag(name = "菜单", description = "菜单相关接口")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/user")
    public Result<List<MenuResponse>> getCurrentUserMenus() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(menuService.getUserMenus(userId));
    }

    @Operation(summary = "获取公共菜单树")
    @GetMapping("/public")
    public Result<List<MenuResponse>> getPublicMenus() {
        return Result.success(menuService.getPublicMenus());
    }

    @Operation(summary = "获取所有菜单树")
    @GetMapping
    public Result<List<MenuResponse>> getAllMenus() {
        return Result.success(menuService.getAllMenus());
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public Result<MenuResponse> getMenuById(@PathVariable Long id) {
        return Result.success(menuService.getMenuById(id));
    }
}
