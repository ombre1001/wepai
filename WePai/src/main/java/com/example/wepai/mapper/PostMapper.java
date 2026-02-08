package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.Post;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    @Select("SELECT p.*, u.nickname, u.avatar_url, u.role " +
            "FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.status = 1 " +
            "AND (#{type} IS NULL OR p.type = #{type}) " +
            "ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectPostsWithUser(Integer type);

    @Insert("INSERT INTO posts (user_id, type, title, content, images, status, created_at) " +
            "VALUES (#{userId}, #{type}, #{title}, #{content}, #{images}, #{status}, #{createdAt})")
    // 如果主键是自增 ID，建议加上 Options
    @Options(useGeneratedKeys = true, keyProperty = "postId")
    int insertPost(Post post);
}

