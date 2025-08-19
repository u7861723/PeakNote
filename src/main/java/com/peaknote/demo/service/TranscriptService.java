package com.peaknote.demo.service;

import okhttp3.Request;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.microsoft.graph.requests.GraphServiceClient;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.entity.MeetingTranscript;
import com.peaknote.demo.repository.MeetingEventRepository;
import com.peaknote.demo.repository.MeetingTranscriptRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TranscriptService {


    private final MeetingSummaryService meetingSummaryService;
    private static final Logger log = LoggerFactory.getLogger(TranscriptService.class);
    @Qualifier("graphClient")
    private final GraphServiceClient<Request> graphClient;
    private final MeetingEventRepository meetingEventRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;

    /**
     * Download transcript content
     *
     * @param userId       User ID
     * @param meetingId    Meeting ID
     * @param transcriptId Transcript ID
     * @param accessToken  Token
     * @return transcript content
     */
    public void downloadTranscriptContent(String userId, String meetingId, String transcriptId, String accessToken) {
        try {
            String url = "https://graph.microsoft.com/beta/users/" + userId
                    + "/onlineMeetings/" + meetingId
                    + "/transcripts/" + transcriptId + "/content";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "text/vtt")
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String content = response.body();
                log.info("✅ Successfully obtained transcript content, preparing to save to database");
                System.out.println(content);
                // Find MeetingEvent based on eventId
                MeetingEvent meetingEvent = meetingEventRepository.findFirstByMeetingIdAndTranscriptStatus(meetingId, "subscribed");
                if (meetingEvent == null) {
                    log.error("❌ Corresponding meeting event not found, eventId={}", meetingId);
                    return;
                }
                String eventId = meetingEvent.getEventId();
                Instant startTime = meetingEvent.getStartTime();
                String summary = meetingSummaryService.generateSummary(startTime,content);
                System.out.println(summary);
                String meetingUrl = meetingEvent.getJoinUrl();
                try {
                    redisTemplate.delete("transcriptCache::" + eventId);
                    redisTemplate.delete("urlEventCache::" + meetingUrl);
                    log.info("✅ delete: transcriptCache::{} and urlEventCache::{}", eventId, meetingUrl);
                } catch (Exception e) {
                    log.error("❌ failed in deleting redis", e);
                }

                // Create and save MeetingTranscript
                MeetingTranscript transcript = new MeetingTranscript();
                transcript.setMeetingEvent(meetingEvent);
                transcript.setContentText(summary);
                transcript.setCreatedAt(Instant.now());
                meetingTranscriptRepository.save(transcript);
                log.info("✅ Transcript saved to database, eventId={}, transcriptId={}", eventId, transcript.getId());

                // ✅ Update event status to saved
                meetingEvent.setTranscriptStatus("saved");
                meetingEventRepository.save(meetingEvent);
                log.info("✅ Updated event {} status to saved", eventId);
            } else {
                log.error("❌ Failed to download transcript, status code: {}, content: {}", response.statusCode(), response.body());
                return;
            }
        } catch (Exception e) {
            log.error("❌ Error downloading transcript: {}", e.getMessage(), e);
            return;
        }
    }

        /**
     * Query all EventId list based on URL and cache
     */
    @Cacheable(value = "urlEventCache", key = "#url")
    public List<String> getEventIdsByUrl(String url) {
        log.info("URL length to query: {}", url.length());
        log.info("URL to query: '{}'", url);
        //String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        byte[] bytes = url.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexBuilder.append(String.format("%02X ", b));
        }
        log.info("URL corresponding byte length: {}", bytes.length);
        log.info("URL hexadecimal: {}", hexBuilder.toString());
        List<String> eventIds = meetingEventRepository.findEventIdsByjoinUrl(url);
        if(eventIds.size()>0){
            log.info("✅ Loaded EventId list from database, url={}, eventIds={}", url, eventIds);
            return eventIds;
        }else{
            log.warn("⚠️ URL not found, url={}", url);
            return null; // Store null to prevent message penetration
        }
    }

    /**
     * Query transcript based on EventId, with caching
     */
    @Cacheable(value = "transcriptCache", key = "#eventId")
    public String getTranscriptByEventId(String eventId) {
        MeetingTranscript transcript = meetingTranscriptRepository.findByMeetingEvent_EventId(eventId);
        if (transcript != null) {
            log.info("✅ Loaded transcript from database, eventId={}", eventId);
            return transcript.getContentText();
        } else {
            log.warn("⚠️ Transcript not found, eventId={}", eventId);
            return null; // Store null to prevent message penetration
        }
    }

        /**
     * Update transcript (database update + delayed double deletion + redisson distributed lock)
     */
    //@CacheEvict(value = "transcriptCache", key = "#eventId", beforeInvocation = true)
    @Transactional
    public void updateTranscript(String eventId, String newContent) {
        String lockKey = "lock:transcript:" + eventId;
        RLock lock = redissonClient.getLock(lockKey);

        try{
            if(lock.tryLock(10,30,TimeUnit.SECONDS)){
                log.info("Successfully obtained distributed lock, eventId={}", eventId);
                redisTemplate.delete("transcriptCache::" + eventId);
                meetingTranscriptRepository.updateContentByEventId(eventId, newContent);
                log.info("✅ Updated transcript in database, eventId={}", eventId);

                // Delayed double deletion to prevent concurrent reading of dirty data
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(500); // Delay 500ms
                        redisTemplate.delete("transcriptCache::" + eventId);
                        log.info("✅ Delayed double deletion completed, eventId={}", eventId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("❌ Delayed double deletion exception", e);
                    }
                });
            }else{
                log.warn("⚠️ Lock acquisition timeout, eventId={}", eventId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ Interrupted while acquiring lock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("✅ Lock released, eventId={}", eventId);
            }
        }
    }
}
