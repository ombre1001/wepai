package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    @Select("SELECT p.*, u.nickname, u.avatar_url, u.role, " +
            "(SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.post_id) as likeCount " +
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

    @Select("SELECT p.*, u.nickname, u.avatar_url, " +
            "(SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.post_id) as likeCount " +
            "FROM posts p LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.status = 1 AND (p.title LIKE CONCAT('%',#{keyword},'%') OR p.content LIKE CONCAT('%',#{keyword},'%'))")
    List<Map<String, Object>> searchPosts(@Param("keyword") String keyword);

    // 实时建议：只查标题
    @Select("SELECT title FROM posts WHERE status = 1 AND title LIKE CONCAT('%',#{keyword},'%') LIMIT 8")
    List<String> getSuggestions(@Param("keyword") String keyword);
}

