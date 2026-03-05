package com.example.wepai.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.dto.AnnouncementDTO;
import com.example.wepai.data.dto.FeedbackDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemMapper {

    // --- 公告相关 ---
    @Insert("INSERT INTO announcements (title, content, type, created_at) VALUES (#{title}, #{content}, #{type}, NOW())")
    int insertAnnouncement(AnnouncementDTO dto);

    @Select("SELECT * FROM announcements WHERE status = 1 ORDER BY created_at DESC")
    List<Map<String, Object>> selectActiveAnnouncements();

    // --- 反馈相关 ---
    @Insert("INSERT INTO feedback (user_id, content, contact, images, created_at) VALUES (#{userId}, #{f.content}, #{f.contact}, #{f.images}, NOW())")
    int insertFeedback(@Param("userId") String userId, @Param("f") FeedbackDTO dto);

    @Select("SELECT f.*, u.nickname FROM feedback f LEFT JOIN user u ON f.user_id = u.cas_id ORDER BY f.created_at DESC")
    List<Map<String, Object>> selectAllFeedbacks(Page<?> page);

    @Select("SELECT  FROM feedback WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Map<String, Object>> selectMyFeedbacks(@Param("userId") String userId);

    @Update("UPDATE feedback SET status = 1 WHERE id = #{feedbackId}")
    int markAsRead(@Param("feedbackId") Long feedbackId);

    // --- 摄影师身份取消 ---
    @Update("UPDATE user SET role = 1 WHERE cas_id = #{casId}")
    int updateRoleToUser(@Param("casId") String casId);
}