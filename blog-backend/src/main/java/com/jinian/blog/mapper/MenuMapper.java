package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Menu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 */
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("""
            SELECT DISTINCT m.* FROM menus m
            INNER JOIN role_menus rm ON m.id = rm.menu_id
            INNER JOIN user_roles ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId} AND m.status = 'ACTIVE'
            ORDER BY m.sort_order
            """)
    List<Menu> selectByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT m.* FROM menus m
            INNER JOIN role_menus rm ON m.id = rm.menu_id
            INNER JOIN roles r ON rm.role_id = r.id
            WHERE r.code = #{roleCode} AND r.deleted = 0 AND m.status = 'ACTIVE'
            ORDER BY m.sort_order
            """)
    List<Menu> selectByRoleCode(@Param("roleCode") String roleCode);

    @Select("""
            SELECT rm.menu_id
            FROM role_menus rm
            WHERE rm.role_id = #{roleId}
            ORDER BY rm.menu_id
            """)
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM role_menus WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO role_menus (role_id, menu_id) VALUES (#{roleId}, #{menuId})")
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    @Select("SELECT * FROM menus WHERE parent_id IS NULL AND status = 'ACTIVE' ORDER BY sort_order")
    List<Menu> selectRootMenus();

    @Select("SELECT * FROM menus WHERE parent_id = #{parentId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);
}
