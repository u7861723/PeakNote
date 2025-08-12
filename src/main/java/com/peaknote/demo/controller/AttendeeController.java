package com.peaknote.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.peaknote.demo.service.MeetingAttendeeService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/attendees")
public class AttendeeController {
        private final MeetingAttendeeService meetingattendeeService;

    public AttendeeController(MeetingAttendeeService meetingattendeeService) {
        this.meetingattendeeService = meetingattendeeService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAttendees(@RequestParam String eventId) {
        Map<String, Object> result = meetingattendeeService.getAttendeesByEventId(eventId);
        return ResponseEntity.ok(result);
    }
}
