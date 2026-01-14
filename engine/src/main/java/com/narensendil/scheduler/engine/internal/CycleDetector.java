package com.narensendil.scheduler.engine.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.narensendil.scheduler.engine.model.Task;

/**
 * Cycle detection / DAG validation using Kahn's algorithm.
 * If we cannot process all nodes, a cycle exists.
 */
public final class CycleDetector {

    private CycleDetector() {}

    public static void ensureAcyclic(GraphBuilder.Graph g) {
        Objects.requireNonNull(g, "graph");

        // Work on a mutable copy of indegrees
        Map<Task, Integer> indegree = new HashMap<>(g.indegree());

        Deque<Task> q = new ArrayDeque<>();
        for (Map.Entry<Task, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) q.add(e.getKey());
        }

        int processed = 0;
        while (!q.isEmpty()) {
            Task cur = q.removeFirst();
            processed++;

            for (Task nxt : g.outgoing().getOrDefault(cur, Set.of())) {
                int newDeg = indegree.merge(nxt, -1, Integer::sum);
                if (newDeg == 0) q.addLast(nxt);
            }
        }

        if (processed != indegree.size()) {
            // Identify a few nodes that are stuck (still have indegree > 0)
            List<String> stuck = indegree.entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .limit(10)
                    .map(e -> e.getKey().id())
                    .toList();

            throw new IllegalArgumentException(
                    "Cycle detected (graph is not a DAG). Stuck tasks include: " + stuck
            );
        }
    }
}
