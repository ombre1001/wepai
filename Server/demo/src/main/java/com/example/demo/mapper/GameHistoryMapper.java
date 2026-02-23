package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.data.po.GameHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameHistoryMapper extends BaseMapper<GameHistory> {

    @Select("SELECT gh.*, g.name as game_name FROM game_history gh " +
            "LEFT JOIN game g ON gh.game_id = g.id " +
            "WHERE gh.user_id = #{userId} ")
    List<GameHistory> getUserGameHistory(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM game_history WHERE user_id = #{userId}")
    Integer getUserGameHistoryCount(@Param("userId") Integer userId);

    @Delete("DELETE FROM game_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);

    @Select("SELECT gh.*, g.name as game_name FROM game_history gh " +
            "LEFT JOIN game g ON gh.game_id = g.id " +
            "WHERE gh.user_id = #{userId} AND gh.game_id = #{gameId} " +
            "ORDER BY gh.score DESC LIMIT 1")
    GameHistory getUserBestScoreByGameId(@Param("userId") Integer userId,
                                         @Param("gameId") Integer gameId);
}
