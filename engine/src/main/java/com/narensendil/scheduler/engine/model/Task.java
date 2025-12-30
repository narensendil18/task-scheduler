package com.narensendil.scheduler.engine.model;

import java.time.Duration;
import java.util.Objects;

public final class Task {

    private final String id;
    private final Duration duration;
    private final int priority;

    public Task(String id, Duration duration, int priority) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Task id cannot be blank");
        }
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
        this.id = id;
        this.duration = duration;
        this.priority = priority;
    }

    public Task(String id, Duration duration) {
        this(id, duration, 0);
    }

    public String id() { return id; }
    public Duration duration() { return duration; }
    public int priority() { return priority; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task t && id.equals(t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
