package com.jinian.blog.service.impl;

import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.MenuResponse;
import com.jinian.blog.entity.Menu;
import com.jinian.blog.mapper.MenuMapper;
import com.jinian.blog.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<MenuResponse> getUserMenus(Long userId) {
        List<Menu> menus = menuMapper.selectByUserId(userId);
        return buildMenuTree(menus, null);
    }

    @Override
    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                        .orderByAsc(Menu::getSortOrder)
        );
        return buildMenuTree(menus, null);
    }

    @Override
    public MenuResponse getMenuById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ResourceNotFoundException("菜单", id);
        }
        return toResponse(menu);
    }

    /**
     * 构建菜单树
     */
    private List<MenuResponse> buildMenuTree(List<Menu> menus, Long parentId) {
        // 按parentId分组
        Map<Long, List<Menu>> menuMap = menus.stream()
                .collect(Collectors.groupingBy(m -> m.getParentId() == null ? 0L : m.getParentId()));

        List<Menu> children = menuMap.getOrDefault(parentId == null ? 0L : parentId, new ArrayList<>());
        return children.stream()
                .map(menu -> {
                    MenuResponse response = toResponse(menu);
                    response.setChildren(buildMenuTree(menus, menu.getId()));
                    return response;
                })
                .toList();
    }

    private MenuResponse toResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .name(menu.getName())
                .code(menu.getCode())
                .path(menu.getPath())
                .icon(menu.getIcon())
                .component(menu.getComponent())
                .sortOrder(menu.getSortOrder())
                .visible(menu.getVisible())
                .build();
    }
}