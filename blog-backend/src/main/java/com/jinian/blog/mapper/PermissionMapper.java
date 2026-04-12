package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.Permission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 */
public interface PermissionMapper extends BaseMapper<Permission> {

    /** 通过角色权限关联查询用户的角色权限（不含用户覆盖） */
    @Select("""
            SELECT p.* FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            INNER JOIN user_roles ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<Permission> selectByUserId(@Param("userId") Long userId);

    /** 查询角色拥有的权限ID列表 */
    @Select("SELECT permission_id FROM role_permissions WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /** 查询角色的权限列表 */
    @Select("""
            SELECT p.* FROM permissions p
            INNER JOIN role_permissions rp ON p.id = rp.permission_id
            WHERE rp.role_id = #{roleId}
            """)
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /** 删除角色的所有权限 */
    @Delete("DELETE FROM role_permissions WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);

    /** 为角色添加权限 */
    @Insert("INSERT INTO role_permissions (role_id, permission_id) VALUES (#{roleId}, #{permissionId}) ON CONFLICT DO NOTHING")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}