package com.example.wepai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.PostDraftListDTO;
import com.example.wepai.data.po.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {



    /**
     * 分页查询用户的草稿列表（status = -1）
     */
    @Select("SELECT post_id AS postId, title, created_at AS createdAt " +
            "FROM posts " +
            "WHERE user_id = #{userId} AND status = -1 " +
            "ORDER BY created_at DESC")
    List<PostDraftListDTO> selectDraftListPaged(Page<PostDraftListDTO> page, @Param("userId") String userId);

    /**
     * 根据ID查询草稿详情（同时校验状态和归属）
     */
    @Select("SELECT * FROM posts WHERE post_id = #{postId} AND user_id = #{userId} AND status = -1")
    @Results({
            @Result(column = "post_id", property = "postId", id = true),
            @Result(column = "user_id", property = "userId"),
            @Result(column = "images", property = "images",
                    typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    })
    Post selectPostDraftById(@Param("postId") Long postId, @Param("userId") String userId);

    /**
     * 物理删除草稿（需校验归属和状态）
     */
    @Delete("DELETE FROM posts WHERE post_id = #{postId} AND user_id = #{userId} AND status = -1")
    int deleteDraftManual(@Param("postId") Long postId, @Param("userId") String userId);


    @Select("SELECT p.*, u.nickname, u.avatar_url, u.role, " +
            "(SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.post_id) as likeCount " +
            "FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.status = 1 " +
            "AND (#{type} IS NULL OR p.type = #{type}) " + // SQL 逻辑兼容 String
            "ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectPostsWithUser(String type); // 参数改为 String

    @Insert("INSERT INTO posts (user_id, type, title, content, images, status, created_at) " +
            "VALUES (#{userId}, #{type}, #{title}, #{content}, " +
            "#{images, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, " + // 指定转换器
            "#{status}, #{createdAt})")
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

    @Select("SELECT p.*, u.nickname, u.avatar_url, u.role " +
            "FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.status = 1 " +
            "AND (#{type} IS NULL OR p.type = #{type}) " +
            "ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectPostsWithUserPaged(Page<?> page, @Param("type") String type);

    @Select("""
        <script>
        SELECT p.*, u.nickname, u.avatar_url, 
        (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.post_id) as likeCount 
        FROM posts p 
        LEFT JOIN user u ON p.user_id = u.cas_id 
        WHERE p.user_id = #{userId} 
        <if test="status != null">
            AND p.status = #{status}
        </if>
        ORDER BY p.created_at DESC
        </script>
        """)
    List<Map<String, Object>> selectMyPosts(
            Page<?> page,
            @Param("userId") String userId,
            @Param("status") Integer status);

    @Select("SELECT p.post_id, p.images, p.title, u.cas_id as user_id, u.nickname, u.avatar_url, u.role, " +
            "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) as totalLikes, " +
            "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id AND user_id = #{currentUserId}) > 0 as isLiked " +
            "FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.status = 1 " +
            "AND (#{type} IS NULL OR p.type = #{type}) " +
            // 新增 keyword 判断：如果 keyword 为 null，则忽略此条件；否则模糊匹配标题或内容
            "AND (#{keyword} IS NULL OR p.title LIKE CONCAT('%',#{keyword},'%') OR p.content LIKE CONCAT('%',#{keyword},'%')) " +
            "ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectPostsSimplified(Page<?> page,
                                                    @Param("type") Integer type,
                                                    @Param("currentUserId") String currentUserId,
                                                    @Param("keyword") String keyword);

    @Select("SELECT p.*, u.nickname, u.avatar_url, u.role, " +
            "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) as totalLikes, " +
            "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id AND user_id = #{currentUserId}) > 0 as isLiked " +
            "FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id " +
            "WHERE p.post_id = #{postId}")
    Map<String, Object> selectPostDetail(@Param("postId") Long postId, @Param("currentUserId") String currentUserId);

    @Update("""
        UPDATE posts SET 
            title = #{title},
            content = #{content},
            type = #{type},
            images = #{images, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler},
            status = #{status}
        WHERE post_id = #{postId} AND user_id = #{userId}
        """)
    int updatePostManual(Post post);

    @Select("""
        <script>
        SELECT 
            p.post_id, 
            p.user_id AS casId, 
            p.title, 
            p.content, 
            p.images, 
            p.type,
            p.status,
            p.created_at,
            u.nickname, 
            u.avatar_url,
            (SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS likeCount,
            (SELECT COUNT(*) FROM post_comments WHERE post_id = p.post_id) AS commentCount
        FROM posts p
        LEFT JOIN user u ON p.user_id = u.cas_id
        WHERE 1=1
        <if test="status != null">
            AND p.status = #{status}
        </if>
        <if test="casId != null and casId != ''">
            AND p.user_id = #{casId}
        </if>
        <if test="type != null">
            AND p.type = #{type}
        </if>
        <if test="keyword != null and keyword != ''">
            AND (p.title LIKE CONCAT('%', #{keyword}, '%') OR p.content LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        ORDER BY p.created_at DESC
        </script>
        """)
    List<Map<String, Object>> selectPostsCombined(
            Page<?> page,
            @Param("casId") String casId,
            @Param("type") Integer type,
            @Param("keyword") String keyword,
            @Param("status") Integer status);

    // 管理员获取所有帖子 (不区分状态和用户)
    @Select("SELECT p.*, u.nickname, u.avatar_url FROM posts p " +
            "LEFT JOIN user u ON p.user_id = u.cas_id ORDER BY p.created_at DESC")
    List<Map<String, Object>> selectAllPostsAdmin(Page<?> page);

    // 管理员强制删除帖子（无 userId 校验）
    @Delete("DELETE FROM posts WHERE post_id = #{postId}")
    int deletePostForce(@Param("postId") Long postId);

    @Delete("DELETE FROM posts WHERE post_id = #{postId} AND user_id = #{userId}")
    int deletePostSecure(@Param("postId") Long postId, @Param("userId") String userId);
}

