package com.example.wepai.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SearchMapper {
    @Insert("INSERT INTO search_history (user_id, keyword, search_type) VALUES (#{userId}, #{keyword}, #{type})")
    void insertHistory(@Param("userId") String userId, @Param("keyword") String keyword, @Param("type") String type);

    @Select("SELECT keyword FROM search_history " +
            "WHERE user_id = #{userId} AND search_type = #{type} " +
            "GROUP BY keyword " +
            "ORDER BY MAX(created_at) DESC " +
            "LIMIT 10")
    List<String> getHistory(@Param("userId") String userId, @Param("type") String type);

    @Delete("DELETE FROM search_history WHERE user_id = #{userId} AND search_type = #{type}")
    void clearHistory(@Param("userId") String userId, @Param("type") String type);
}
