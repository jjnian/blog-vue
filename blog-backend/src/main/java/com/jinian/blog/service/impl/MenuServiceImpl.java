package com.jinian.blog.service.impl;

import com.jinian.blog.common.exception.ResourceNotFoundException;
import com.jinian.blog.dto.response.MenuResponse;
import com.jinian.blog.entity.Menu;
import com.jinian.blog.entity.Role;
import com.jinian.blog.mapper.MenuMapper;
import com.jinian.blog.mapper.RoleMapper;
import com.jinian.blog.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMapper roleMapper;

    @Override
    public List<MenuResponse> getUserMenus(Long userId) {
        List<Menu> menus = expandWithAncestors(menuMapper.selectByUserId(userId));
        return buildMenuTree(menus, null);
    }

    @Override
    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, "ACTIVE")
                        .orderByAsc(Menu::getSortOrder)
        );
        return buildMenuTree(expandWithAncestors(menus), null);
    }

    @Override
    public List<MenuResponse> getPublicMenus() {
        return getMenusByRoleCode("GUEST");
    }

    @Override
    public List<MenuResponse> getMenusByRoleCode(String roleCode) {
        List<Menu> menus = expandWithAncestors(menuMapper.selectByRoleCode(roleCode));
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

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void updateRoleMenus(Long roleId, List<Long> menuIds) {
        Role role = roleMapper.selectById(roleId);
        if (role == null || (role.getDeleted() != null && role.getDeleted() == 1)) {
            throw new ResourceNotFoundException("角色", roleId);
        }

        menuMapper.deleteByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        Set<Long> distinctIds = menuIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        for (Long menuId : distinctIds) {
            menuMapper.insertRoleMenu(roleId, menuId);
        }
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

    private List<Menu> expandWithAncestors(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Menu> allMenus = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, "ACTIVE")
        ).stream().collect(Collectors.toMap(Menu::getId, menu -> menu, (left, right) -> left, LinkedHashMap::new));

        Map<Long, Menu> ordered = new LinkedHashMap<>();
        for (Menu menu : menus) {
            addMenuWithAncestors(menu, allMenus, ordered);
        }
        return new ArrayList<>(ordered.values());
    }

    private void addMenuWithAncestors(Menu menu, Map<Long, Menu> allMenus, Map<Long, Menu> ordered) {
        if (menu == null || ordered.containsKey(menu.getId())) {
            return;
        }

        if (menu.getParentId() != null) {
            addMenuWithAncestors(allMenus.get(menu.getParentId()), allMenus, ordered);
        }
        ordered.put(menu.getId(), menu);
    }
}
