package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Insert("INSERT IGNORE INTO user (cas_id, name) VALUES (#{casId}, #{name})")
    int insertUser(User user);

    @Select("SELECT * FROM user WHERE cas_id = #{casId}")
    User getUserById(String casId);

    @Select("SELECT * FROM user WHERE name = #{name}")
    User getUserByName(String name);

    @Select("SELECT cas_id FROM user WHERE name = #{name}")
    String getUserId(String name);


    @Update("UPDATE user SET " +
            "nickname = #{user.nickname}, " +
            "avatar_url = #{user.avatarUrl}, " +
            "sex = #{user.sex}, " +
            "phone = #{user.phone}, " +
            "detail = #{user.detail} " +
            "WHERE cas_id = #{user.casId}")
    int updateUser(@Param("user") User user);

    @Update("UPDATE user SET role = #{role} WHERE cas_id = #{casId}")
    int updateUserRole(@Param("casId") String casId, @Param("role") Integer role);




}
