package com.peaknote.demo.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.peaknote.demo.entity.MeetingInstance;
import com.peaknote.demo.entity.MeetingInstance.TranscriptStatus;
import com.peaknote.demo.entity.TeamsUser;
import com.peaknote.demo.repository.MeetingInstanceRepository;
import com.peaknote.demo.repository.UserRepository;

@Service
public class MeetingInstanceSyncService {

    private final GraphServiceClient<?> graphClient;
    private final UserRepository userRepository;
    private final MeetingInstanceRepository meetingRepo;
    
    public MeetingInstanceSyncService(
        @org.springframework.beans.factory.annotation.Qualifier("graphClient") GraphServiceClient<?> graphClient,
        UserRepository userRepository,
        MeetingInstanceRepository meetingRepo
    ) {
        System.out.println("🔧 DemoApplication constructor called");
        this.graphClient = graphClient;
        this.userRepository = userRepository;
        this.meetingRepo = meetingRepo;
    }

    public void syncFutureMeetings() {
        System.out.println("🟡 Running syncFutureMeetings...");
        List<TeamsUser> users = userRepository.findAll();
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(7);

        for (TeamsUser user : users) {
            try {
                List<Event> events = graphClient
                    .users(user.getOid())
                    .calendarView()
                    .buildRequest(
                        Arrays.asList(
                            new QueryOption("startDateTime", start.toString()),
                            new QueryOption("endDateTime", end.toString())
                        )
                    )
                    .select("id,subject,start,end,seriesMasterId,onlineMeeting,isOnlineMeeting")
                    .top(50)
                    .get()
                    .getCurrentPage();

                for (Event event : events) {
                    System.out.println("📦 Event Raw JSON: " + graphClient.getSerializer().serializeObject(event));
                    if (!event.isOnlineMeeting) {
                        System.out.println("❌ Skipping event " + event.id + " (no onlineMeeting)");
                        continue;
                    }

                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                    .toFormatter()
                    .withZone(ZoneOffset.UTC); // Or use ZoneId.of("UTC") based on your actual situation;

                    MeetingInstance instance = new MeetingInstance();
                    instance.seteventId(event.id);
                    instance.setSeriesMasterId(event.seriesMasterId);
                    instance.setJoinUrl(event.onlineMeeting.joinUrl);
                    instance.setCreatedBy(user.getOid());
                    instance.setStartTime(Instant.from(formatter.parse(event.start.dateTime)));
                    instance.setEndTime(Instant.from(formatter.parse(event.end.dateTime)));
                    instance.setTranscriptStatus(TranscriptStatus.NONE);
                    instance.setCreatedAt(Instant.now());

                    if (!meetingRepo.existsById(event.id)) {
                        meetingRepo.save(instance);
                    }
                }

            } catch (Exception ex) {
                System.err.println("Failed to sync for user: " + user.getOid());
                ex.printStackTrace();
            }
        }
    }
}
