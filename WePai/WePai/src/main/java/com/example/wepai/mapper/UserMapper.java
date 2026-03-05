package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.po.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Insert("INSERT IGNORE INTO user (cas_id, name) VALUES (#{casId}, #{name})")
    int insertUser(User user);

    @Select("SELECT * FROM user WHERE cas_id = #{casId}")
    @Results({
            @Result(column = "cas_id", property = "casId", id = true),
            @Result(column = "avatar_url", property = "avatarUrl")
    })
    User getUserById(String casId);

    @Select("SELECT * FROM user WHERE name = #{name}")
    User getUserByName(String name);

    @Select("SELECT cas_id FROM user WHERE name = #{name}")
    String getUserId(String name);


    @Update("""
        <script>
        UPDATE user
        <set>
            cas_id = cas_id, <if test="user.nickname != null">nickname = #{user.nickname},</if>
            <if test="user.avatarUrl != null">avatar_url = #{user.avatarUrl},</if>
            <if test="user.sex != null">sex = #{user.sex},</if>
            <if test="user.phone != null">phone = #{user.phone},</if>
            <if test="user.detail != null">detail = #{user.detail},</if>
            <if test="user.agreement != null">agreement = #{user.agreement},</if>
        </set>
        WHERE cas_id = #{user.casId}
        </script>
        """)
    int updateUser(@Param("user") User user);

    @Update("UPDATE user SET role = #{role} WHERE cas_id = #{casId}")
    int updateUserRole(@Param("casId") String casId, @Param("role") Integer role);

    // 查询所有用户列表
    @Select("SELECT cas_id, nickname, avatar_url, phone, status + 0 AS status, role + 0 AS role FROM user")
    List<Map<String, Object>> selectAllUsers(Page<?> page);

    // 更新用户状态（如封禁）
    @Update("UPDATE user SET status = #{status} WHERE cas_id = #{userId}")
    int updateUserStatus(@Param("userId") String userId, @Param("status") Integer status);

    @Select("SELECT status FROM user WHERE cas_id = #{casId}")
    User selectUsersStatus(String casId);
}
