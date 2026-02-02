package com.narensendil.scheduler.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleRequestDto;
import com.narensendil.scheduler.server.dto.ScheduleDtos.ScheduleResultDto;
import com.narensendil.scheduler.server.service.ScheduleService;

@RestController
@RequestMapping("/api")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) {
        this.service = service;
    }

    @PostMapping("/schedule")
    public ResponseEntity<ScheduleResultDto> schedule(@RequestBody ScheduleRequestDto req) {
        return ResponseEntity.ok(service.schedule(req));
    }
}
