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
        List<MeetingAttendee> attendees = meetingAttendeeRepository.findByMeetingEvent_EventId(eventId);

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
