package com.example.demo.service;

import com.example.demo.data.po.NumberGuess;
import com.example.demo.data.vo.Result;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class NumberGuessService {

    @Resource
    private GameService gameService;

    // 存储用户游戏状态
    private Map<String, NumberGuess> sessions = new HashMap<>();

    /**
     * 开始新游戏
     */
    public ResponseEntity<Result> startGame(String userId) {
        try {
            Random random = new Random();
            int targetNumber = random.nextInt(100) + 1; // 1-100的随机数
            int maxAttempts = 10;

            NumberGuess session = new NumberGuess(targetNumber, maxAttempts);
            sessions.put(userId, session);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "游戏开始！猜一个1-100之间的数字，你有" + maxAttempts + "次机会");
            result.put("maxAttempts", maxAttempts);

            return Result.success(result, "游戏开始成功");
        } catch (Exception e) {
           throw new RuntimeException("开始失败");
        }
    }

    /**
     * 猜数字
     */
    public ResponseEntity<Result> guessNumber(String userId, Integer guess) {
        try {
            NumberGuess session = sessions.get(userId);
            if (session == null) {
                throw new RuntimeException("请开始");
            }

            if (guess == null || guess < 1 || guess > 100) {
                throw new RuntimeException("请输入1-100之间的数字");
            }

            session.incrementAttempts();
            int attempts = session.getAttempts();
            int target = session.getTargetNumber();

            Map<String, Object> result = new HashMap<>();
            result.put("attempts", attempts);
            result.put("guess", guess);

            if (guess < target) {
                result.put("message", "猜小了！");
                return Result.success(result, "继续猜");
            } else if (guess > target) {
                result.put("message", "猜大了！");
                return Result.success(result, "继续猜");
            } else {

                int score = calculateScore(attempts, session.getMaxAttempts());


                Map<String, Object> gameData = new HashMap<>();
                gameData.put("targetNumber", target);
                gameData.put("attempts", attempts);
                gameData.put("maxAttempts", session.getMaxAttempts());

                gameService.saveGameRecord(Integer.valueOf(userId), 1, score, gameData.toString());


                sessions.remove(userId);

                result.put("message", "恭喜你猜对了！");
                result.put("score", score);
                result.put("targetNumber", target);

                return Result.success(result, "游戏胜利");
            }
        } catch (Exception e) {
            throw new RuntimeException("失败");
        }
    }

    /**
     * 计算得分
     */
    private int calculateScore(int attempts, int maxAttempts) {
        int baseScore = 100;
        int penalty = (attempts - 1) * 10; // 每多尝试一次扣10分
        return Math.max(baseScore - penalty, 10); // 最低10分
    }

    /**
     * 获取游戏状态
     */
    public ResponseEntity<Result> getGameStatus(String userId) {
        try {
            NumberGuess session = sessions.get(userId);
            if (session == null) {
                throw new RuntimeException("无游戏");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("attempts", session.getAttempts());
            result.put("maxAttempts", session.getMaxAttempts());


            return Result.success(result, "获取游戏状态成功");
        } catch (Exception e) {
            throw new RuntimeException("获取失败");
        }
    }
}



