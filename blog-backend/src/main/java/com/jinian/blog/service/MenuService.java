package com.jinian.blog.service;

import com.jinian.blog.dto.response.MenuResponse;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取用户菜单树
     */
    List<MenuResponse> getUserMenus(Long userId);

    /**
     * 获取所有菜单树
     */
    List<MenuResponse> getAllMenus();

    /**
     * 获取公共菜单树
     */
    List<MenuResponse> getPublicMenus();

    /**
     * 根据角色编码获取菜单树
     */
    List<MenuResponse> getMenusByRoleCode(String roleCode);

    /**
     * 获取菜单详情
     */
    MenuResponse getMenuById(Long id);

    /**
     * 获取角色已分配的菜单 ID
     */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /**
     * 更新角色的菜单分配
     */
    void updateRoleMenus(Long roleId, List<Long> menuIds);
}
