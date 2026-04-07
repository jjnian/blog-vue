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
     * 获取菜单详情
     */
    MenuResponse getMenuById(Long id);
}