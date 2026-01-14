package com.narensendil.scheduler.engine;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.ScheduleRequest;
import com.narensendil.scheduler.engine.model.ScheduleResult;
import com.narensendil.scheduler.engine.model.ScheduledTask;
import com.narensendil.scheduler.engine.model.Strategy;
import com.narensendil.scheduler.engine.model.Task;

public class SchedulerEngineTest {

    @Test
    void emptyTasks_succeedsWithZeroMakespan() {
        SchedulerEngine engine = new SchedulerEngine();

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                List.of(),
                List.of(),
                new ScheduleOptions(Strategy.FIFO, 1)
        ));

        assertTrue(result.success());
        assertFalse(result.metrics().hasCycle());
        assertEquals(Duration.ZERO, result.metrics().makespan());
        assertTrue(result.scheduledTasks().isEmpty());
    }

    @Test
    void duplicateTaskIds_throwIllegalArgumentException() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1),
                new Task("A", Duration.ofSeconds(2), 5)
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> engine.schedule(new ScheduleRequest(
                        tasks, List.of(), new ScheduleOptions(Strategy.FIFO, 1)
                ))
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    void missingDependencyReference_throwsIllegalArgumentException() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("X", "A") // X does not exist
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> engine.schedule(new ScheduleRequest(
                        tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
                ))
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    void disconnectedComponents_scheduleAllTasks() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1),
                new Task("B", Duration.ofSeconds(1), 1),
                new Task("C", Duration.ofSeconds(1), 1),
                new Task("D", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "B"),
                new Dependency("C", "D")
        );

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
        ));

        assertTrue(result.success());
        assertEquals(Duration.ofSeconds(4), result.metrics().makespan());

        List<String> order = result.scheduledTasks().stream()
                .map(ScheduledTask::taskId)
                .collect(Collectors.toList());

        assertEquals(4, order.size());
        assertEquals(4, order.stream().distinct().count());
        assertTrue(order.indexOf("A") < order.indexOf("B"));
        assertTrue(order.indexOf("C") < order.indexOf("D"));
    }

    @Test
    void longChain_schedulesInExactOrder() {
        SchedulerEngine engine = new SchedulerEngine();

        List<Task> tasks = List.of(
                new Task("A", Duration.ofSeconds(1), 1),
                new Task("B", Duration.ofSeconds(1), 1),
                new Task("C", Duration.ofSeconds(1), 1),
                new Task("D", Duration.ofSeconds(1), 1),
                new Task("E", Duration.ofSeconds(1), 1)
        );

        List<Dependency> deps = List.of(
                new Dependency("A", "B"),
                new Dependency("B", "C"),
                new Dependency("C", "D"),
                new Dependency("D", "E")
        );

        ScheduleResult result = engine.schedule(new ScheduleRequest(
                tasks, deps, new ScheduleOptions(Strategy.FIFO, 1)
        ));

        assertTrue(result.success());
        assertEquals(
                List.of("A", "B", "C", "D", "E"),
                result.scheduledTasks().stream().map(ScheduledTask::taskId).toList()
        );
    }
}
