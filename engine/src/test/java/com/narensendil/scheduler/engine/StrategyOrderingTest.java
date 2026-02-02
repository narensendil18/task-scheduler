package com.narensendil.scheduler.engine;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.ScheduleRequest;
import com.narensendil.scheduler.engine.model.ScheduleResult;
import com.narensendil.scheduler.engine.model.ScheduledTask;
import com.narensendil.scheduler.engine.model.Strategy;
import com.narensendil.scheduler.engine.model.Task;

public class StrategyOrderingTest {

    @Test
    void fifoAndPriority_differentOrder_sameMakespan() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(3), 1),
                new Task("B", Duration.ofSeconds(2), 5),
                new Task("C", Duration.ofSeconds(1), 10)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "C"),
                new Dependency("B", "C")
        );

        ScheduleResult fifo = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
        ));

        ScheduleResult priority = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.PRIORITY, 1)
        ));

        assertEquals(
                List.of("A", "B", "C"),
                fifo.scheduledTasks().stream().map(ScheduledTask::taskId).toList()
        );

        assertEquals(
                List.of("B", "A", "C"),
                priority.scheduledTasks().stream().map(ScheduledTask::taskId).toList()
        );

        assertEquals(fifo.metrics().makespan(), priority.metrics().makespan());
    }

    @Test
    void priorityTie_isDeterministic() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 5),
                new Task("B", Duration.ofSeconds(1), 5),
                new Task("C", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "C"),
                new Dependency("B", "C")
        );

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.PRIORITY, 1)
        ));

        // Deterministic secondary ordering (taskOrder)
        assertEquals(
                List.of("A", "B", "C"),
                result.scheduledTasks().stream().map(ScheduledTask::taskId).toList()
        );
    }
}
