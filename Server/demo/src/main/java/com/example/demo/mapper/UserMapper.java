package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.data.po.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(String id);

    @Select("SELECT * FROM user WHERE username = #{userName}")
    User getUserByUsername(String username);

    @Select("SELECT id FROM user WHERE username = #{userName}")
    Integer getUserId(String userName);

    @Select("SELECT password FROM user WHERE username = #{userName}")
    String getPassword(String userName);

    @Update("UPDATE user SET username = #{user.username}, email = #{user.email}, phone = #{user.phone} WHERE id = #{user.id}")
    int updateUser(@Param("user") User user);

    @Update("UPDATE user SET password = #{user.password} WHERE id = #{user.id}")
    int updateUserPassword(@Param("user") User user);

    @Update("UPDATE user SET status = 0 WHERE id = #{user.id}") // 0 表示禁用
    int updateUserStatus(@Param("user") User user);

    // 添加检查邮箱是否存在的方法
    @Select("SELECT COUNT(*) FROM user WHERE email = #{email} AND id != #{id}")
    Integer checkEmailExists(@Param("email") String email, @Param("id") String id);

    @Delete("DELETE FROM user WHERE id = #{userId}")
    int deleteUserById(@Param("userId") String userId);
}
