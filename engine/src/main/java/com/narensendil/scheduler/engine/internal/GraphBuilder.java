package com.narensendil.scheduler.engine.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.Task;

/**
 * Builds an internal directed graph from tasks + dependencies.
 *
 * Edge direction: prerequisite -> dependent
 * indegree[task] = number of prerequisites remaining
 */
public final class GraphBuilder {

    private GraphBuilder() {}

    public record Graph(
            Map<Task, Set<Task>> outgoing,
            Map<Task, Integer> indegree,
            Map<String, Task> byId,
            List<Task> taskOrder
    ) {}

    public static Graph build(List<Task> tasks, List<Dependency> dependencies) {
        Objects.requireNonNull(tasks, "tasks");
        Objects.requireNonNull(dependencies, "dependencies");

        // Preserve input order for FIFO tie-breaking
        List<Task> taskOrder = List.copyOf(tasks);

        Map<String, Task> byId = new HashMap<>();
        for (Task t : tasks) {
            Task prev = byId.put(t.id(), t);
            if (prev != null) {
                throw new IllegalArgumentException("Duplicate task id: " + t.id());
            }
        }

        Map<Task, Set<Task>> outgoing = new HashMap<>();
        Map<Task, Integer> indegree = new HashMap<>();

        for (Task t : tasks) {
            outgoing.put(t, new HashSet<>());
            indegree.put(t, 0);
        }

        for (Dependency d : dependencies) {
            // Assumed accessors:
            String prereqId = d.prerequisite();
            String dependentId = d.dependent();

            Task prereq = byId.get(prereqId);
            Task dependent = byId.get(dependentId);

            if (prereq == null) {
                throw new IllegalArgumentException("Dependency references missing prerequisite task id: " + prereqId);
            }
            if (dependent == null) {
                throw new IllegalArgumentException("Dependency references missing dependent task id: " + dependentId);
            }
            if (prereq.equals(dependent)) {
                throw new IllegalArgumentException("Task cannot depend on itself: " + prereqId);
            }

            // Avoid double-counting indegree if duplicate Dependency objects exist
            boolean added = outgoing.get(prereq).add(dependent);
            if (added) {
                indegree.put(dependent, indegree.get(dependent) + 1);
            }
        }

        return new Graph(
                Collections.unmodifiableMap(outgoing),
                Collections.unmodifiableMap(indegree),
                Collections.unmodifiableMap(byId),
                taskOrder
        );
    }
}
