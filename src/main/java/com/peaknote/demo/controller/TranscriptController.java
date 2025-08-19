package com.peaknote.demo.controller;

import com.peaknote.demo.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/transcript")
public class TranscriptController {

    private final TranscriptService transcriptService;

    /**
     * Query all meeting transcripts by URL
     */
    // @GetMapping("/by-url")
    // public List<String> getTranscriptsByUrl(@RequestParam String url) {
    //     List<String> eventIds = transcriptService.getEventIdsByUrl(url);
    //     if (eventIds == null || eventIds.isEmpty()) {
    //         return List.of(); // Return empty list, frontend will get empty array []
    //     }
    //     return eventIds.stream()
    //         .map(transcriptService::getTranscriptByEventId)
    //         .collect(Collectors.toList());
    // }
    @GetMapping("/by-url")
    public Map<String, String> getTranscriptsByUrl(@RequestParam String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                throw new IllegalArgumentException("URL parameter cannot be empty");
            }
            
            //String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);

            List<String> eventIds = transcriptService.getEventIdsByUrl(url);
            if (eventIds == null || eventIds.isEmpty()) {
                return Map.of("transcript", "");
            }
            String transcript = transcriptService.getTranscriptByEventId(eventIds.get(0));
            return Map.of("eventId", eventIds.get(0), "transcript", transcript);
        } catch (Exception e) {
            System.err.println("❌ Failed to get meeting transcript: url=" + url + ", error=" + e.getMessage());
            e.printStackTrace();
            return Map.of("transcript", "", "error", "Failed to get meeting transcript: " + e.getMessage());
        }
    }

    /**
     * Update meeting transcript
     */
    @PostMapping("/update")
    public String updateTranscript(@RequestParam String eventId, @RequestParam String content) {
        try {
            if (eventId == null || eventId.trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID cannot be empty");
            }
            if (content == null) {
                throw new IllegalArgumentException("Content cannot be empty");
            }
            
            transcriptService.updateTranscript(eventId, content);
            return "✅ success";
        } catch (Exception e) {
            System.err.println("❌ Failed to update meeting transcript: eventId=" + eventId + ", error=" + e.getMessage());
            e.printStackTrace();
            return "❌ Update failed: " + e.getMessage();
        }
    }

    
}
