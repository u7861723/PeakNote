package com.peaknote.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.peaknote.demo.entity.MeetingAttendee;
import com.peaknote.demo.repository.MeetingAttendeeRepository;

@Service
public class MeetingAttendeeService {
    private final MeetingAttendeeRepository meetingAttendeeRepository;

    public MeetingAttendeeService(MeetingAttendeeRepository meetingAttendeeRepository) {
        this.meetingAttendeeRepository = meetingAttendeeRepository;
    }

    public Map<String, Object> getAttendeesByEventId(String eventId) {
        // Validate eventId parameter
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        
        List<MeetingAttendee> attendees = meetingAttendeeRepository.findByMeetingEvent_EventId(eventId);
        
        // Check if attendees list is null (database error)
        if (attendees == null) {
            throw new RuntimeException("Database error occurred while searching for attendees");
        }
        
        // Check if no attendees found for the given eventId
        if (attendees.isEmpty()) {
            throw new RuntimeException("No attendees found for event ID: " + eventId);
        }

        List<Map<String, String>> users = attendees.stream().map(a -> {
            Map<String, String> map = new HashMap<>();
            map.put("email", a.getUserEmail());
            map.put("displayName", a.getDisplayName());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("attendees", users);
        return response;
    }
}
