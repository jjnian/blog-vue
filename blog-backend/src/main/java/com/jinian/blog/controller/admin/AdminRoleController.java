package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jinian.blog.common.Result;
import com.jinian.blog.entity.Role;
import com.jinian.blog.mapper.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 角色管理
 */
@Tag(name = "管理后台-角色", description = "角色管理接口")
@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleMapper roleMapper;

    @Operation(summary = "获取角色列表")
    @GetMapping
    public Result<List<Role>> getRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, 0);
        return Result.success(roleMapper.selectList(wrapper));
    }

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<Role> createRole(@RequestBody Role role) {
        roleMapper.insert(role);
        return Result.success(role);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleMapper.updateById(role);
        return Result.success(roleMapper.selectById(id));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.success();
    }
}