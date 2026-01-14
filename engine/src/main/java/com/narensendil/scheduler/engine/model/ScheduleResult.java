package com.narensendil.scheduler.engine.model;

import java.util.List;

public final class ScheduleResult {

    private final boolean success;
    private final List<ScheduledTask> scheduledTasks;
    private final ScheduleMetrics metrics;
    private final List<Diagnostic> diagnostics;

    public ScheduleResult(
            boolean success,
            List<ScheduledTask> scheduledTasks,
            ScheduleMetrics metrics,
            List<Diagnostic> diagnostics
    ) {
        this.success = success;
        this.scheduledTasks = List.copyOf(scheduledTasks);
        this.metrics = metrics;
        this.diagnostics = List.copyOf(diagnostics);
    }

    public boolean success() { return success; }
    public List<ScheduledTask> scheduledTasks() { return scheduledTasks; }
    public ScheduleMetrics metrics() { return metrics; }
    public List<Diagnostic> diagnostics() { return diagnostics; }
}
