package com.example.demo.data.po;

public class NumberGuess {
    private int targetNumber;
    private int attempts;
    private int maxAttempts;
    private long startTime;

    public NumberGuess(int targetNumber, int maxAttempts) {
        this.targetNumber = targetNumber;
        this.maxAttempts = maxAttempts;
        this.attempts = 0;
        this.startTime = System.currentTimeMillis();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getDuration() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
}

