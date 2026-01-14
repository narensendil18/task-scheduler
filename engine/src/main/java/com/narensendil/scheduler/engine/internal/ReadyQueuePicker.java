package com.narensendil.scheduler.engine.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.Strategy;
import com.narensendil.scheduler.engine.model.Task;

/**
 * Decides which READY task to pick next when multiple tasks are available.
 * - FIFO: respects original input order as much as possible
 * - PRIORITY: higher priority first (ties broken by input order, then id)
 */
public final class ReadyQueuePicker {

    private ReadyQueuePicker() {}

    public static ReadyQueue create(List<Task> taskOrder, ScheduleOptions options) {
        Strategy s = (options == null || options.strategy() == null)
                ? Strategy.FIFO
                : options.strategy();

        return switch (s) {
            case PRIORITY -> new PriorityReadyQueue(taskOrder);
            case FIFO -> new FifoReadyQueue();
            default -> new FifoReadyQueue(); // safe fallback if you add more strategies later
        };
    }

    /** Small wrapper so SchedulerEngine doesn't care which queue implementation is used. */
    public interface ReadyQueue {
        void add(Task t);
        Task poll();
        boolean isEmpty();
    }

    private static final class FifoReadyQueue implements ReadyQueue {
        private final Deque<Task> q = new ArrayDeque<>();

        @Override public void add(Task t) { q.addLast(t); }
        @Override public Task poll() { return q.pollFirst(); }
        @Override public boolean isEmpty() { return q.isEmpty(); }
    }

    private static final class PriorityReadyQueue implements ReadyQueue {
        private final Map<String, Integer> orderIndex; // id -> input order index
        private final PriorityQueue<Task> pq;

        private PriorityReadyQueue(List<Task> taskOrder) {
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < taskOrder.size(); i++) {
                idx.put(taskOrder.get(i).id(), i);
            }
            this.orderIndex = idx;

            this.pq = new PriorityQueue<>((a, b) -> {
                // higher priority first
                int cmp = Integer.compare(b.priority(), a.priority());
                if (cmp != 0) return cmp;

                // tie-break by original input order (stable-ish)
                cmp = Integer.compare(
                        orderIndex.getOrDefault(a.id(), Integer.MAX_VALUE),
                        orderIndex.getOrDefault(b.id(), Integer.MAX_VALUE)
                );
                if (cmp != 0) return cmp;

                // final tie-break by id (deterministic)
                return a.id().compareTo(b.id());
            });
        }

        @Override public void add(Task t) { pq.add(t); }
        @Override public Task poll() { return pq.poll(); }
        @Override public boolean isEmpty() { return pq.isEmpty(); }
    }
}
