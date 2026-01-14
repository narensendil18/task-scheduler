package com.narensendil.scheduler.engine.model;

public final class ScheduleOptions {

    private final Strategy strategy;
    private final int parallelism;

    public ScheduleOptions(Strategy strategy, int parallelism) {
        if (parallelism < 1) {
            throw new IllegalArgumentException("parallelism must be >= 1");
        }
        this.strategy = strategy;
        this.parallelism = parallelism;
    }

    public static ScheduleOptions defaults() {
        return new ScheduleOptions(Strategy.FIFO, 1);
    }

    public Strategy strategy() { return strategy; }
    public int parallelism() { return parallelism; }
}
