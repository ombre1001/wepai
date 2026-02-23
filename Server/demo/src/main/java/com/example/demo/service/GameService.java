package com.example.demo.service;

import com.example.demo.data.po.Game;
import com.example.demo.data.po.GameHistory;
import com.example.demo.data.vo.Result;
import com.example.demo.mapper.GameMapper;
import com.example.demo.mapper.GameHistoryMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    @Resource
    private GameMapper gameMapper;

    @Resource
    private GameHistoryMapper gameHistoryMapper;

    @PostConstruct
    public void initGames() {
        try {

            if (gameMapper.selectCount(null) == 0) {

                Game game1 = new Game();
                game1.setName("猜数字");
                game1.setDescription("猜一个1-100之间的数字，测试你的直觉和运气！");
                gameMapper.insert(game1);

                Game game2 = new Game();
                game2.setName("2048");
                game2.setDescription("数字合并游戏，挑战2048！");
                gameMapper.insert(game2);

                System.out.println("游戏数据初始化完成");
            }
        } catch (Exception e) {
            System.err.println("游戏数据初始化失败: ");
        }
    }

    /**
     * 获取所有游戏列表
     */
    public ResponseEntity<Result> getAllGames() {
        try {
            List<Game> games = gameMapper.getAllGames();
            return Result.success(games, "获取游戏列表成功");
        } catch (Exception e) {
            throw new RuntimeException("获取游戏列表失败");
        }
    }


    /**
     * 保存游戏记录
     */
    @Transactional
    public ResponseEntity<Result> saveGameRecord(Integer userId, Integer gameId,
                                                 Integer score,
                                                 String additionalInfo) {
        try {
            GameHistory history = new GameHistory();
            history.setUserId(userId);
            history.setGameId(gameId);
            history.setScore(score);
            history.setAdditionalInfo(additionalInfo);


            int result = gameHistoryMapper.insert(history);
            if (result <= 0) {
                throw new RuntimeException("保存游戏记录失败");
            }

            return Result.success(null, "游戏记录保存成功");
        } catch (Exception e) {
            throw new RuntimeException("保存游戏记录失败");
        }
    }

    /**
     * 获取用户游戏历史记录
     */
    public ResponseEntity<Result> getUserGameHistory(Integer userId) {
        try {
            List<GameHistory> historyList = gameHistoryMapper.getUserGameHistory(userId);

            // 统计信息
            Integer totalGames = gameHistoryMapper.getUserGameHistoryCount(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("history", historyList);
            result.put("totalGames", totalGames);

            return Result.success(result, "获取游戏历史记录成功");
        } catch (Exception e) {
            throw new RuntimeException("获取游戏记录失败");
        }
    }

    /**
     * 清空用户游戏历史记录
     */
    @Transactional
    public ResponseEntity<Result> clearUserGameHistory(Integer userId) {
        try {
            int result = gameHistoryMapper.deleteByUserId(userId);
            return Result.success(null, "已清空 " + result + " 条游戏记录");
        } catch (Exception e) {
            throw new RuntimeException("清空游戏记录失败");
        }
    }

    /**
     * 获取用户指定游戏的最高分记录
     */
    /**
     * 获取用户在指定游戏中的最高分记录
     */
    public ResponseEntity<Result> getUserBestScore(Integer userId, Integer gameId) {
        try {
            GameHistory bestScore = gameHistoryMapper.getUserBestScoreByGameId(userId, gameId);
            if (bestScore == null) {
                throw new RuntimeException("暂无游戏记录");
            }
            return Result.success(bestScore, "获取最高分成功");
        } catch (Exception e) {
            throw new RuntimeException("获取失败");
        }
    }
}