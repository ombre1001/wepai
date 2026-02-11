package com.example.wepai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.wepai.data.po.Photographer;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PhotographerMapper extends BaseMapper<Photographer> {

    // 获取评分平均值
    @Select("SELECT COALESCE(AVG(score), 0) FROM ratings WHERE target_id = #{casId}")
    Double getAverageScore(@Param("casId") String casId);

    // 联合查询获取摄影师列表（包含User基础信息）
    @Select("SELECT u.cas_id, u.nickname, u.avatar_url, p.type, p.order_count " +
            "FROM user u JOIN photographer p ON u.cas_id = p.cas_id " +
            "WHERE u.role = 2")
    List<Map<String, Object>> getPhotographerList();


    @Select("SELECT p.cas_id, p.style, p.equipment, p.type, " +
            "(SELECT COUNT(*) FROM orders WHERE photographer_id = p.cas_id AND status = 3) as orderCount " +
            "FROM photographer p WHERE p.cas_id = #{casId}")
    @Results({
            @Result(column = "cas_id", property = "casId", id = true),
            @Result(column = "style", property = "style"),
            @Result(column = "equipment", property = "equipment"),
            @Result(column = "type", property = "type"),
            @Result(column = "orderCount", property = "orderCount") // 对应子查询结果
    })
    Photographer getPhotographerById(@Param("casId") String casId);

    // 2. 核心修复：使用 ON DUPLICATE KEY UPDATE 解决 400 冲突报错
    // 即使记录已存在，也只会执行更新而不会报错
    @Insert("INSERT INTO photographer (cas_id, style, equipment, type) " +
            "VALUES (#{casId}, #{style}, #{equipment}, #{type}) " +
            "ON DUPLICATE KEY UPDATE " +
            "style = #{style}, equipment = #{equipment}, type = #{type}")
    int upsertPhotographer(Photographer photographer);

    @Select("SELECT u.cas_id, u.nickname, u.avatar_url, p.type, p.style " +
            "FROM user u JOIN photographer p ON u.cas_id = p.cas_id " +
            "WHERE u.role = 2 AND (u.nickname LIKE CONCAT('%',#{keyword},'%') OR p.style LIKE CONCAT('%',#{keyword},'%'))")
    List<Map<String, Object>> searchPhotographers(@Param("keyword") String keyword);

    // 实时建议：只查昵称
    @Select("SELECT u.nickname FROM user u WHERE u.role = 2 AND u.nickname LIKE CONCAT('%',#{keyword},'%') LIMIT 8")
    List<String> getSuggestions(@Param("keyword") String keyword);

    /**
     * 摄影师接单量排行榜（按完成订单数倒序）
     *
     * @param limit 返回前多少名，不传则由 Service 设默认值
     */
    @Select("""
            SELECT 
                u.cas_id,
                u.nickname,
                u.avatar_url,
                p.type,
                COALESCE(COUNT(o.order_id), 0) AS orderCount
            FROM photographer p
            JOIN user u ON u.cas_id = p.cas_id
            LEFT JOIN orders o 
                ON o.photographer_id = p.cas_id 
               AND o.status = 3   -- 只统计已完成订单
            WHERE u.role = 2
            GROUP BY u.cas_id, u.nickname, u.avatar_url, p.type
            ORDER BY orderCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> getOrderRanking(@Param("limit") int limit);

    /**
     * 摄影师评分排行榜（按平均评分倒序，其次按评价次数倒序）
     *
     * @param limit 返回前多少名
     */
    @Select("""
            SELECT 
                u.cas_id,
                u.nickname,
                u.avatar_url,
                p.type,
                COALESCE(AVG(r.score), 0) AS avgScore,
                COUNT(r.rating_id)        AS ratingCount
            FROM photographer p
            JOIN user u ON u.cas_id = p.cas_id
            LEFT JOIN ratings r 
                ON r.target_id = p.cas_id
            WHERE u.role = 2
            GROUP BY u.cas_id, u.nickname, u.avatar_url, p.type
            ORDER BY avgScore DESC, ratingCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> getRatingRanking(@Param("limit") int limit);

    @Select("SELECT u.cas_id, u.nickname, u.avatar_url, p.type, p.order_count " +
            "FROM user u JOIN photographer p ON u.cas_id = p.cas_id " +
            "WHERE u.role = 2")
    List<Map<String, Object>> getPhotographerListPaged(Page<?> page);


}