package com.narensendil.scheduler.server.service;

import org.springframework.stereotype.Service;

import com.narensendil.scheduler.engine.SchedulerEngine;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleRequestDto;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleResultDto;
import com.narensendil.scheduler.server.mapper.ScheduleMapper;

@Service
public class ScheduleService {

    private final SchedulerEngine engine = new SchedulerEngine();
    private final ScheduleMapper mapper = new ScheduleMapper();

    public ScheduleResultDto schedule(ScheduleRequestDto req) {
        return mapper.toDto(engine.schedule(mapper.toDomain(req)));
    }
}
