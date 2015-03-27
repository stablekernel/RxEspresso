package com.rosshambrick.rxespresso;

public enum LogLevel {
    NONE(0),
    DEBUG(1),
    VERBOSE(2);
    private int levelValue;

    LogLevel(int levelValue) {
        this.levelValue = levelValue;
    }

    public boolean atOrAbove(LogLevel logLevel) {
        return this.levelValue >= logLevel.levelValue;
    }
}
