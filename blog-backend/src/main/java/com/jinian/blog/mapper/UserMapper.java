package com.jinian.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jinian.blog.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE username = #{username} AND deleted = 0")
    User selectByUsername(String username);

    @Select("SELECT * FROM users WHERE email = #{email} AND deleted = 0")
    User selectByEmail(String email);

    @Insert("INSERT INTO user_roles (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM user_roles WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") Long userId);
}