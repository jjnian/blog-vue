package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("""
            SELECT p.* FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN user_roles ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<Permission> selectByUserId(@Param("userId") Long userId);
}