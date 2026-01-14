package com.narensendil.scheduler.engine;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.ScheduleRequest;
import com.narensendil.scheduler.engine.model.ScheduleResult;
import com.narensendil.scheduler.engine.model.Strategy;
import com.narensendil.scheduler.engine.model.Task;

public class CycleDetectionTest {

    @Test
    void simpleCycle_detectedAndRejected() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1),
                new Task("B", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "B"),
                new Dependency("B", "A")
        );

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
        ));

        assertFalse(result.success());
        assertTrue(result.metrics().hasCycle());
        assertEquals(Duration.ZERO, result.metrics().makespan());
        assertTrue(result.scheduledTasks().isEmpty());

        assertTrue(result.diagnostics().stream()
                .anyMatch(d -> d.message().toLowerCase().contains("cycle")));
    }

    @Test
void selfDependency_isRejectedBeforeScheduling() {
    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new Dependency("A", "A")
    );
    assertTrue(ex.getMessage().toLowerCase().contains("self"));
}


    @Test
    void partialCycle_failsWholeSchedule() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1),
                new Task("B", Duration.ofSeconds(1), 1),
                new Task("C", Duration.ofSeconds(1), 1),
                new Task("D", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "B"),
                new Dependency("B", "A"),
                new Dependency("C", "D")
        );

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
        ));

        assertFalse(result.success());
        assertTrue(result.metrics().hasCycle());
        assertTrue(result.scheduledTasks().isEmpty());
    }
}
