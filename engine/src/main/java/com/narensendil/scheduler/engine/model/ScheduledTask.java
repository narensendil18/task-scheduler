package com.narensendil.scheduler.engine.model;

import java.time.Duration;

public final class ScheduledTask {

    private final String taskId;
    private final int worker;
    private final Duration start;
    private final Duration end;

    public ScheduledTask(
            String taskId,
            int worker,
            Duration start,
            Duration end
    ) {
        this.taskId = taskId;
        this.worker = worker;
        this.start = start;
        this.end = end;
    }

    public String taskId() { return taskId; }
    public int worker() { return worker; }
    public Duration start() { return start; }
    public Duration end() { return end; }
}
