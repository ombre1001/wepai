package com.example.wepai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.wepai.data.po.Photographer;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PhotographerMapper extends BaseMapper<Photographer> {

    // 获取评分平均值
    @Select("SELECT AVG(score) FROM ratings WHERE photographer_id = #{casId}")
    Double getAverageScore(String casId);

    // 联合查询获取摄影师列表（包含User基础信息）
    @Select("SELECT u.cas_id, u.nickname, u.avatar_url, p.type, p.order_count " +
            "FROM user u JOIN photographer p ON u.cas_id = p.cas_id " +
            "WHERE u.role = 2")
    List<Map<String, Object>> getPhotographerList();


    @Select("SELECT p.*, " +
            "(SELECT COUNT(*) FROM orders WHERE photographer_id = p.cas_id AND status = 3) as orderCount " +
            "FROM photographer p WHERE p.cas_id = #{casId}")
    @Results({
            @Result(column = "cas_id", property = "casId", id = true),
            @Result(column = "style", property = "style"),
            @Result(column = "equipment", property = "equipment"),
            @Result(column = "type", property = "type")
    })
    Photographer getPhotographerById(@Param("casId") String casId);

    // 2. 核心修复：使用 ON DUPLICATE KEY UPDATE 解决 400 冲突报错
    // 即使记录已存在，也只会执行更新而不会报错
    @Insert("INSERT INTO photographer (cas_id, style, equipment, type) " +
            "VALUES (#{casId}, #{style}, #{equipment}, #{type}) " +
            "ON DUPLICATE KEY UPDATE style = #{style}, equipment = #{equipment}, type = #{type}")
    int upsertPhotographer(Photographer photographer);

    @Insert("INSERT INTO photographer (cas_id, style, equipment, type) " +
            "VALUES (#{casId}, #{style}, #{equipment}, #{type}, #{orderCount})")
    int insertPhotographer(Photographer photographer);

    @Update("UPDATE photographer SET style = #{style}, equipment = #{equipment}, type = #{type} WHERE cas_id = #{casId}")
    int updatePhotographer(Photographer photographer);

}