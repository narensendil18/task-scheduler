package com.narensendil.scheduler.server.mapper;

import java.time.Duration;
import java.util.List;

import com.narensendil.scheduler.engine.model.Dependency;
import com.narensendil.scheduler.engine.model.ScheduleOptions;
import com.narensendil.scheduler.engine.model.ScheduleRequest;
import com.narensendil.scheduler.engine.model.ScheduleResult;
import com.narensendil.scheduler.engine.model.Strategy;
import com.narensendil.scheduler.engine.model.Task;
import com.narensendil.scheduler.server.dto.ScheduleDtos.DiagnosticDto;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleRequestDto;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleResultDto;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduledTaskDto;

public final class ScheduleMapper {

    public ScheduleRequest toDomain(ScheduleRequestDto dto) {
        List<Task> tasks = dto.tasks().stream()
                .map(t -> new Task(
                        t.id(),
                        Duration.ofMinutes(t.durationMinutes()),
                        t.priority()
                ))
                .toList();

        List<Dependency> deps = dto.dependencies().stream()
                .map(d -> new Dependency(d.prerequisite(), d.dependent()))
                .toList();

        ScheduleOptions options = null;
        if (dto.options() != null && dto.options().strategy() != null && !dto.options().strategy().isBlank()) {
            Strategy strategy = Strategy.valueOf(dto.options().strategy().trim().toUpperCase());
            options = new ScheduleOptions(strategy,1);
        }

        return new ScheduleRequest(tasks, deps, options);
    }

    public ScheduleResultDto toDto(ScheduleResult result) {
        List<ScheduledTaskDto> scheduled = result.scheduledTasks().stream()
                .map(st -> new ScheduledTaskDto(
                        st.taskId(),
                        st.worker(),
                        st.start().toMinutes(),
                        st.end().toMinutes()
                ))
                .toList();

        List<DiagnosticDto> diags = result.diagnostics().stream()
                .map(d -> new DiagnosticDto(d.severity().name(), d.message()))
                .toList();

        return new ScheduleResultDto(
                result.success(),
                scheduled,
                result.metrics().makespan().toMinutes(),
                result.metrics().hasCycle(),
                diags
        );
    }
}
