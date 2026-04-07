package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Menu;
import org.apache.ibatis.annotations.Param;
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

    @Select("SELECT * FROM menus WHERE parent_id IS NULL AND status = 'ACTIVE' ORDER BY sort_order")
    List<Menu> selectRootMenus();

    @Select("SELECT * FROM menus WHERE parent_id = #{parentId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);
}