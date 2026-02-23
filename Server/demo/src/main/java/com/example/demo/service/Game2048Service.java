package com.example.demo.service;

import com.example.demo.data.po.Game2048;
import com.example.demo.data.vo.Result;
import com.example.demo.service.GameService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Game2048Service {

    @Resource
    private GameService gameService;


    private Map<String, Game2048> activeGames = new HashMap<>();
    private static final int GRID_SIZE = 4;

    /**
     * 开始新游戏
     */
    public ResponseEntity<Result> startNewGame(String userId) {
        try {
            int[][] grid = new int[GRID_SIZE][GRID_SIZE];

            // 初始生成两个数字
            addRandomTile(grid);
            addRandomTile(grid);

            Game2048 session = new Game2048(grid, 0, false, false);
            activeGames.put(userId, session);

            return Result.success(createResponse(session), "游戏开始");
        } catch (Exception e) {
            throw new RuntimeException("开始游戏失败");
        }
    }

    /**
     * 移动
     */
    public ResponseEntity<Result> move(String userId, String direction) {
        try {
            Game2048 session = activeGames.get(userId);
            if (session == null) {
                throw new RuntimeException("游戏未开始");
            }

            if (session.gameOver || session.won) {
                throw new RuntimeException("请开始新游戏");
            }

            int[][] grid = copyGrid(session.grid);
            boolean moved = false;

            switch (direction.toLowerCase()) {
                case "up": moved = moveUp(grid); break;
                case "down": moved = moveDown(grid); break;
                case "left": moved = moveLeft(grid); break;
                case "right": moved = moveRight(grid); break;
            }

            if (moved) {
                session.grid = grid;
                addRandomTile(grid);

                // 检查游戏状态
                session.won = checkWin(grid);
                session.gameOver = checkGameOver(grid);

                // 游戏结束时保存记录
                if (session.gameOver || session.won) {
                    saveRecord(userId, session);
                }

                return Result.success(createResponse(session), "移动成功");
            } else {
                throw new RuntimeException("无法移动");
            }
        } catch (Exception e) {
            throw new RuntimeException("无法移动");
        }
    }

    /**
     * 重新开始
     */
    public ResponseEntity<Result> restart(String userId) {
        activeGames.remove(userId);
        return startNewGame(userId);
    }

    /**
     * 获取游戏状态
     */
    public ResponseEntity<Result> getStatus(String userId) {
        Game2048 session = activeGames.get(userId);
        if (session == null) {
            throw new RuntimeException("游戏为空");
        }
        return Result.success(createResponse(session), "获取状态成功");
    }

/**
 *向上
 */

    private boolean moveUp(int[][] grid) {
        boolean moved = false;
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE - 1; row++) {
                if (grid[row][col] != 0) {
                    for (int nextRow = row + 1; nextRow < GRID_SIZE; nextRow++) {
                        if (grid[nextRow][col] != 0) {
                            if (grid[row][col] == grid[nextRow][col]) {
                                grid[row][col] *= 2;
                                grid[nextRow][col] = 0;
                                moved = true;
                            }
                            break;
                        }
                    }
                }
            }
            for (int row = 0; row < GRID_SIZE; row++) {
                if (grid[row][col] == 0) {
                    for (int nextRow = row + 1; nextRow < GRID_SIZE; nextRow++) {
                        if (grid[nextRow][col] != 0) {
                            grid[row][col] = grid[nextRow][col];
                            grid[nextRow][col] = 0;
                            moved = true;
                            break;
                        }
                    }
                }
            }
        }
        return moved;
    }
    /**
     *向下
     */
    private boolean moveDown(int[][] grid) {
        boolean moved = false;
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = GRID_SIZE - 1; row > 0; row--) {
                if (grid[row][col] != 0) {
                    for (int nextRow = row - 1; nextRow >= 0; nextRow--) {
                        if (grid[nextRow][col] != 0) {
                            if (grid[row][col] == grid[nextRow][col]) {
                                grid[row][col] *= 2;
                                grid[nextRow][col] = 0;
                                moved = true;
                            }
                            break;
                        }
                    }
                }
            }
            for (int row = GRID_SIZE - 1; row >= 0; row--) {
                if (grid[row][col] == 0) {
                    for (int nextRow = row - 1; nextRow >= 0; nextRow--) {
                        if (grid[nextRow][col] != 0) {
                            grid[row][col] = grid[nextRow][col];
                            grid[nextRow][col] = 0;
                            moved = true;
                            break;
                        }
                    }
                }
            }
        }
        return moved;
    }
    /**
     *向左
     */
    private boolean moveLeft(int[][] grid) {
        boolean moved = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE - 1; col++) {
                if (grid[row][col] != 0) {
                    for (int nextCol = col + 1; nextCol < GRID_SIZE; nextCol++) {
                        if (grid[row][nextCol] != 0) {
                            if (grid[row][col] == grid[row][nextCol]) {
                                grid[row][col] *= 2;
                                grid[row][nextCol] = 0;
                                moved = true;
                            }
                            break;
                        }
                    }
                }
            }
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col] == 0) {
                    for (int nextCol = col + 1; nextCol < GRID_SIZE; nextCol++) {
                        if (grid[row][nextCol] != 0) {
                            grid[row][col] = grid[row][nextCol];
                            grid[row][nextCol] = 0;
                            moved = true;
                            break;
                        }
                    }
                }
            }
        }
        return moved;
    }
    /**
     *向右
     */
    private boolean moveRight(int[][] grid) {
        boolean moved = false;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = GRID_SIZE - 1; col > 0; col--) {
                if (grid[row][col] != 0) {
                    for (int nextCol = col - 1; nextCol >= 0; nextCol--) {
                        if (grid[row][nextCol] != 0) {
                            if (grid[row][col] == grid[row][nextCol]) {
                                grid[row][col] *= 2;
                                grid[row][nextCol] = 0;
                                moved = true;
                            }
                            break;
                        }
                    }
                }
            }
            for (int col = GRID_SIZE - 1; col >= 0; col--) {
                if (grid[row][col] == 0) {
                    for (int nextCol = col - 1; nextCol >= 0; nextCol--) {
                        if (grid[row][nextCol] != 0) {
                            grid[row][col] = grid[row][nextCol];
                            grid[row][nextCol] = 0;
                            moved = true;
                            break;
                        }
                    }
                }
            }
        }
        return moved;
    }

    private void addRandomTile(int[][] grid) {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            Random random = new Random();
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            grid[cell[0]][cell[1]] = random.nextDouble() < 0.9 ? 2 : 4;
        }
    }

    private boolean checkWin(int[][] grid) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkGameOver(int[][] grid) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 0) return false;
            }
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if ((j < GRID_SIZE - 1 && grid[i][j] == grid[i][j + 1]) ||
                        (i < GRID_SIZE - 1 && grid[i][j] == grid[i + 1][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[][] copyGrid(int[][] grid) {
        int[][] newGrid = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(grid[i], 0, newGrid[i], 0, GRID_SIZE);
        }
        return newGrid;
    }

    private int getMaxTile(int[][] grid) {
        int max = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] > max) max = grid[i][j];
            }
        }
        return max;
    }

    private void saveRecord(String userId, Game2048 session) {
        try {
            int maxTile = getMaxTile(session.grid);
            gameService.saveGameRecord(
                    Integer.valueOf(userId),
                    2, // 2048游戏ID
                    session.score,
                    "{\"maxTile\":" + maxTile + ",\"won\":" + session.won + "}"
            );
        } catch (Exception e) {
        }
    }

    private Map<String, Object> createResponse(Game2048 session) {
        Map<String, Object> response = new HashMap<>();
        response.put("grid", session.grid);
        response.put("score", session.score);
        response.put("gameOver", session.gameOver);
        response.put("won", session.won);
        response.put("maxTile", getMaxTile(session.grid));
        return response;
    }


}
