package com.peaknote.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
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
        try {
            Map<String, Object> result = meetingattendeeService.getAttendeesByEventId(eventId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            // Handle invalid parameter errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Bad Request");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("eventId", eventId);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            // Handle business logic errors (no attendees found, database errors)
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Not Found");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("eventId", eventId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            // Handle unexpected errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "An unexpected error occurred while retrieving attendees");
            errorResponse.put("eventId", eventId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
