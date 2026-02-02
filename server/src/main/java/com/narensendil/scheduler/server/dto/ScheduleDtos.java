package com.narensendil.scheduler.server.dto;

import java.util.List;

public final class ScheduleDtos 
{
    private ScheduleDtos() {}

    public record ScheduleRequestDto(
            List<TaskDto> tasks,
            List<DependencyDto> dependencies,
            OptionsDto options
    ) {}

    public record TaskDto(String id, long durationMinutes, int priority) {}

    public record DependencyDto(String prerequisite, String dependent) {}

    public record OptionsDto(String strategy) {}

    public record ScheduleResultDto(
            boolean success,
            List<ScheduledTaskDto> scheduledTasks,
            long makespanMinutes,
            boolean hasCycle,
            List<DiagnosticDto> diagnostics
    ) {}

    public record ScheduledTaskDto(String taskId, int worker, long startMinutes, long endMinutes) {}

    public record DiagnosticDto(String level, String message) {}
}
