package com.example.wepai.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.po.PostComment;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface InteractionMapper {

    // --- 点赞相关 ---
    @Insert("INSERT INTO post_likes (post_id, user_id, created_at) VALUES (#{postId}, #{userId}, NOW())")
    int insertLike(@Param("postId") Long postId, @Param("userId") String userId);

    @Delete("DELETE FROM post_likes WHERE post_id = #{postId} AND user_id = #{userId}")
    int deleteLike(@Param("postId") Long postId, @Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM post_likes WHERE post_id = #{postId}")
    int countLikesByPost(@Param("postId") Long postId);

    // 查询某用户(userId)发布的所有帖子，总共获得了多少个赞
    @Select("SELECT COUNT(*) FROM post_likes l " +
            "INNER JOIN posts p ON l.post_id = p.post_id " +
            "WHERE p.user_id = #{userId}")
    int countTotalLikesReceived(@Param("userId") String userId);

    // --- 评论相关 ---
    @Insert("INSERT INTO post_comments (post_id, user_id, content, created_at) " +
            "VALUES (#{comment.postId}, #{comment.userId}, #{comment.content}, #{comment.createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "comment.id")
    int insertComment(@Param("comment") PostComment comment);

    @Delete("DELETE FROM post_comments WHERE id = #{id} AND user_id = #{userId}")
    int deleteComment(@Param("id") Long id, @Param("userId") String userId);

    @Select("SELECT user_id FROM post_comments WHERE id = #{id}")
    String getCommentAuthorId(@Param("id") Long id);

    // 获取某帖子的评论列表（包含评论者的昵称和头像）
    @Select("SELECT c.*, u.nickname, u.avatar_url FROM post_comments c " +
            "LEFT JOIN user u ON c.user_id = u.cas_id " +
            "WHERE c.post_id = #{postId} ORDER BY c.created_at DESC")
    List<Map<String, Object>> getCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.nickname, u.avatar_url " +
            "FROM post_comments c " +
            "LEFT JOIN user u ON c.user_id = u.cas_id " +
            "WHERE c.post_id = #{postId} " +
            "ORDER BY c.created_at DESC")
    List<Map<String, Object>> selectCommentsByPostIdPaged(Page<?> page, @Param("postId") Long postId);

    // 管理员强制删除评论（无 userId 校验）
    @Delete("DELETE FROM post_comments WHERE id = #{commentId}")
    int deleteCommentForce(@Param("commentId") Long commentId);

    @Select("""
        SELECT 
            c.id as commentId, 
            c.post_id as postId, 
            c.content, 
            c.created_at as createdAt,
            u.nickname as userName, 
            u.avatar_url as userAvatar,
            p.title as postTitle
        FROM post_comments c
        LEFT JOIN user u ON c.user_id = u.cas_id
        LEFT JOIN posts p ON c.post_id = p.post_id
        ORDER BY c.created_at DESC
        """)
    List<Map<String, Object>> selectAllCommentsAdmin(Page<?> page);
}



