package com.narensendil.scheduler.engine.model;

import java.time.Duration;

public final class ScheduleMetrics {

    private final Duration makespan;
    private final boolean hasCycle;

    public ScheduleMetrics(Duration makespan, boolean hasCycle) {
        this.makespan = makespan;
        this.hasCycle = hasCycle;
    }

    public Duration makespan() 
    { 
        return makespan; 
    }
    public boolean hasCycle() 
    { 
        return hasCycle; 
    }
}
