package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.UserPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户专属权限 Mapper
 */
public interface UserPermissionMapper extends BaseMapper<UserPermission> {

    @Select("SELECT * FROM user_permissions WHERE user_id = #{userId}")
    List<UserPermission> selectByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM user_permissions WHERE user_id = #{userId} AND permission_id = #{permissionId}")
    int deleteByUserAndPermission(@Param("userId") Long userId, @Param("permissionId") Long permissionId);

    @Delete("DELETE FROM user_permissions WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Long userId);
}
