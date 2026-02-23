package com.example.demo.data.po;

public class Game2048 {

        public int[][] grid;
        public int score;
        public boolean gameOver;
        public boolean won;

        public Game2048(int[][] grid, int score, boolean gameOver, boolean won) {
            this.grid = grid;
            this.score = score;
            this.gameOver = gameOver;
            this.won = won;
        }
}

