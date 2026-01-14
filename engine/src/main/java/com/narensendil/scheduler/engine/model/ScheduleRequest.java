package com.narensendil.scheduler.engine.model;

import java.util.List;

public final class ScheduleRequest {

    private final List<Task> tasks;
    private final List<Dependency> dependencies;
    private final ScheduleOptions options;

    public ScheduleRequest(
            List<Task> tasks,
            List<Dependency> dependencies,
            ScheduleOptions options
    ) {
        this.tasks = List.copyOf(tasks);
        this.dependencies = List.copyOf(dependencies);
        this.options = options;
    }

    public List<Task> tasks() { return tasks; }
    public List<Dependency> dependencies() { return dependencies; }
    public ScheduleOptions options() { return options; }
}
