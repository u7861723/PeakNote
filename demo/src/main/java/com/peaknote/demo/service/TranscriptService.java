package com.peaknote.demo.service;

import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.microsoft.graph.requests.GraphServiceClient;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.entity.MeetingTranscript;
import com.peaknote.demo.repository.MeetingEventRepository;
import com.peaknote.demo.repository.MeetingTranscriptRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Service
public class TranscriptService {

    @Autowired
    MeetingSummaryService meetingSummaryService;

    private static final Logger log = LoggerFactory.getLogger(TranscriptService.class);
    private final GraphServiceClient<Request> graphClient;
    private final MeetingEventRepository meetingEventRepository;
    private final MeetingTranscriptRepository meetingTranscriptRepository;

    public TranscriptService(@Qualifier("graphClient") GraphServiceClient<Request> graphClient,
                             MeetingEventRepository meetingEventRepository,
                             MeetingTranscriptRepository meetingTranscriptRepository) {
        this.graphClient = graphClient;
        this.meetingEventRepository = meetingEventRepository;
        this.meetingTranscriptRepository = meetingTranscriptRepository;
    }
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
                String summary = meetingSummaryService.generateSummary(content);
                System.out.println(summary);
                // 根据 eventId 查找 MeetingEvent
                MeetingEvent meetingEvent = meetingEventRepository.findByMeetingIdAndTranscriptStatus(meetingId, "subscribed");
                if (meetingEvent == null) {
                    log.error("❌ 未找到对应会议事件，eventId={}", meetingId);
                    return;
                }
                String eventId = meetingEvent.getEventId();

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
}
