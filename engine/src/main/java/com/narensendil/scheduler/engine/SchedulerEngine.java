package com.narensendil.scheduler.engine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.narensendil.scheduler.engine.internal.CycleDetector;
import com.narensendil.scheduler.engine.internal.GraphBuilder;
import com.narensendil.scheduler.engine.internal.ReadyQueuePicker;
import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.Diagnostic;
import com.narensendil.scheduler.engine.model.ScheduleMetrics;
import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.ScheduleRequest;
import com.narensendil.scheduler.engine.model.ScheduleResult;
import com.narensendil.scheduler.engine.model.ScheduledTask;
import com.narensendil.scheduler.engine.model.Task;

public final class SchedulerEngine {

    /**
     * Schedules tasks subject to dependency constraints using Kahn's algorithm.
     * Strategy (FIFO vs PRIORITY) only affects tie-breaking when multiple tasks are ready.
     */
    public ScheduleResult schedule(ScheduleRequest request) {
        Objects.requireNonNull(request, "request");

        List<Task> tasks = Objects.requireNonNull(request.tasks(), "tasks");
        List<Dependency> deps = Objects.requireNonNull(request.dependencies(), "dependencies");
        ScheduleOptions options = request.options(); // may be null

        List<Diagnostic> diagnostics = new ArrayList<>();
        List<ScheduledTask> scheduled = new ArrayList<>(tasks.size());

        // 1) Build graph (also validates duplicate ids + missing dependency refs)
        GraphBuilder.Graph g = GraphBuilder.build(tasks, deps);

        // 2) Validate DAG
        try {
            CycleDetector.ensureAcyclic(g);
        } catch (IllegalArgumentException cycle) {
            diagnostics.add(Diagnostic.error(cycle.getMessage()));
            ScheduleMetrics metrics = new ScheduleMetrics(Duration.ZERO, true);
            return new ScheduleResult(false, List.of(), metrics, diagnostics);
        }

        // 3) Run Kahn's algorithm
        Map<Task, Integer> indegree = new HashMap<>(g.indegree());
        Map<Task, Set<Task>> outgoing = g.outgoing();

        ReadyQueuePicker.ReadyQueue ready = ReadyQueuePicker.create(g.taskOrder(), options);

        // seed ready queue in deterministic order
        for (Task t : g.taskOrder()) {
            if (indegree.getOrDefault(t, 0) == 0) {
                ready.add(t);
            }
        }

        Duration cursor = Duration.ZERO;
        int worker = 0; // single worker for now
        int processed = 0;

        while (!ready.isEmpty()) {
            Task cur = ready.poll();
            if (cur == null) break;

            processed++;

            Duration start = cursor;
            Duration end = cursor.plus(cur.duration());
            scheduled.add(new ScheduledTask(cur.id(), worker, start, end));
            cursor = end;

            for (Task nxt : outgoing.getOrDefault(cur, Set.of())) {
                int newDeg = indegree.merge(nxt, -1, Integer::sum);
                if (newDeg == 0) {
                    ready.add(nxt);
                }
            }
        }

        // Safety check (shouldn't happen after cycle check, but keep it safe)
        if (processed != tasks.size()) {
            diagnostics.add(Diagnostic.error(
                    "Not all tasks were scheduled. Check for missing tasks or invalid dependencies."
            ));
            ScheduleMetrics metrics = new ScheduleMetrics(cursor, false);
            return new ScheduleResult(false, scheduled, metrics, diagnostics);
        }

        ScheduleMetrics metrics = new ScheduleMetrics(cursor, false);
        return new ScheduleResult(true, scheduled, metrics, diagnostics);
    }


}
