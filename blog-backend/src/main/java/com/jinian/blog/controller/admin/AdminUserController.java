package com.jinian.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jinian.blog.common.Result;
import com.jinian.blog.dto.response.PageResponse;
import com.jinian.blog.dto.response.UserResponse;
import com.jinian.blog.entity.Role;
import com.jinian.blog.entity.User;
import com.jinian.blog.mapper.RoleMapper;
import com.jinian.blog.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理后台 - 用户管理
 */
@Tag(name = "管理后台-用户", description = "用户管理接口")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Operation(summary = "获取用户列表")
    @GetMapping
    public Result<PageResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }

        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(pageParam, wrapper);

        List<UserResponse> records = userPage.getRecords().stream()
                .map(user -> {
                    List<Role> roles = roleMapper.selectByUserId(user.getId());
                    List<String> roleCodes = roles.stream().map(Role::getCode).collect(Collectors.toList());
                    return UserResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .bio(user.getBio())
                            .status(user.getStatus())
                            .createdAt(user.getCreatedAt())
                            .lastLoginAt(user.getLastLoginAt())
                            .roles(roleCodes)
                            .build();
                })
                .collect(Collectors.toList());

        return Result.success(PageResponse.<UserResponse>builder()
                .records(records)
                .total(userPage.getTotal())
                .pages(userPage.getPages())
                .current(userPage.getCurrent())
                .size(userPage.getSize())
                .build());
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            return Result.error(404, "用户不存在");
        }

        List<Role> roles = roleMapper.selectByUserId(id);
        List<String> roleCodes = roles.stream().map(Role::getCode).collect(Collectors.toList());

        return Result.success(UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roleCodes)
                .build());
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "分配角色")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 删除原有角色
        userMapper.deleteUserRoles(id);

        // 分配新角色
        for (Long roleId : roleIds) {
            userMapper.insertUserRole(id, roleId);
        }

        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }
}