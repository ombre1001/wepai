package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Insert("INSERT INTO user (cas_id, name) VALUES (#{casId}, #{name}) " +
            "ON DUPLICATE KEY UPDATE name = #{name}")
    int insertUser(User user);

    @Select("SELECT * FROM user WHERE cas_id = #{casId}")
    User getUserById(String casId);

    @Select("SELECT * FROM user WHERE name = #{name}")
    User getUserByName(String name);

    @Select("SELECT cas_id FROM user WHERE name = #{name}")
    String getUserId(String name);


    @Update("UPDATE user SET name = #{user.username}, email = #{user.email}, phone = #{user.phone} WHERE cas_id = #{user.casId}")
    int updateUser(@Param("user") User user);




}
