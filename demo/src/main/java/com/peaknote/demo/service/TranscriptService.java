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
import java.net.URLDecoder;
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
     * 下载 transcript 内容
     *
     * @param userId       用户 ID
     * @param meetingId    会议 ID
     * @param transcriptId transcript ID
     * @param accessToken  token
     * @return transcript 内容
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
                log.info("✅ 成功获取 transcript 内容，准备保存数据库");
                System.out.println(content);
                // 根据 eventId 查找 MeetingEvent
                MeetingEvent meetingEvent = meetingEventRepository.findByMeetingIdAndTranscriptStatus(meetingId, "subscribed");
                if (meetingEvent == null) {
                    log.error("❌ 未找到对应会议事件，eventId={}", meetingId);
                    return;
                }
                String eventId = meetingEvent.getEventId();
                Instant startTime = meetingEvent.getStartTime();
                String summary = meetingSummaryService.generateSummary(startTime,content);
                System.out.println(summary);

                // 创建并保存 MeetingTranscript
                MeetingTranscript transcript = new MeetingTranscript();
                transcript.setMeetingEvent(meetingEvent);
                transcript.setContentText(summary);
                transcript.setCreatedAt(Instant.now());
                meetingTranscriptRepository.save(transcript);
                log.info("✅ 已保存 transcript 到数据库，eventId={}, transcriptId={}", eventId, transcript.getId());

                // ✅ 更新事件状态为 saved
                meetingEvent.setTranscriptStatus("saved");
                meetingEventRepository.save(meetingEvent);
                log.info("✅ 已更新事件 {} 状态为 saved", eventId);
            } else {
                log.error("❌ 下载 transcript 失败，状态码: {}, 内容: {}", response.statusCode(), response.body());
                return;
            }
        } catch (Exception e) {
            log.error("❌ 下载 transcript 出错: {}", e.getMessage(), e);
            return;
        }
    }

        /**
     * 根据 URL 查询所有 EventId 列表，并缓存
     */
    @Cacheable(value = "urlEventCache", key = "#url")
    public List<String> getEventIdsByUrl(String url) {
        log.info("准备查询的 URL 长度: {}", url.length());
        log.info("准备查询的 URL: '{}'", url);
        //String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        byte[] bytes = url.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexBuilder.append(String.format("%02X ", b));
        }
        log.info("URL 对应字节长度: {}", bytes.length);
        log.info("URL 十六进制: {}", hexBuilder.toString());
        List<String> eventIds = meetingEventRepository.findEventIdsByjoinUrl(url);
        if(eventIds.size()>0){
            log.info("✅ 从数据库加载 EventId 列表，url={}, eventIds={}", url, eventIds);
            return eventIds;
        }else{
            log.warn("⚠️ 未找到 url，url={}", url);
            return null; // 存入null，防止消息穿透
        }
    }

    /**
     * 根据 EventId 查询 transcript，带缓存
     */
    @Cacheable(value = "transcriptCache", key = "#eventId")
    public String getTranscriptByEventId(String eventId) {
        MeetingTranscript transcript = meetingTranscriptRepository.findByMeetingEvent_EventId(eventId);
        if (transcript != null) {
            log.info("✅ 从数据库加载 transcript，eventId={}", eventId);
            return transcript.getContentText();
        } else {
            log.warn("⚠️ 未找到 transcript，eventId={}", eventId);
            return null; // 存入null，防止消息穿透
        }
    }

        /**
     * 更新 transcript（数据库更新 + 延迟双删 + redisson分布式锁）
     */
    @CacheEvict(value = "transcriptCache", key = "#eventId", beforeInvocation = true)
    @Transactional
    public void updateTranscript(String eventId, String newContent) {
        String lockKey = "lock:transcript:" + eventId;
        RLock lock = redissonClient.getLock(lockKey);

        try{
            if(lock.tryLock(10,30,TimeUnit.SECONDS)){
                log.info("成功获取分布式锁，eventId={}", eventId);
                redisTemplate.delete("transcriptCache::" + eventId);
                meetingTranscriptRepository.updateContentByEventId(eventId, newContent);
                log.info("✅ 已更新数据库中的 transcript，eventId={}", eventId);

                // 延迟双删，防止并发读取到脏数据
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(500); // 延迟 500ms
                        redisTemplate.delete("transcriptCache::" + eventId);
                        log.info("✅ 延迟双删完成，eventId={}", eventId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("❌ 延迟双删异常", e);
                    }
                });
            }else{
                log.warn("⚠️ 获取锁超时，eventId={}", eventId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("❌ 加锁时被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("✅ 已释放锁，eventId={}", eventId);
            }
        }
    }
}
