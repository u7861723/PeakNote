package com.peaknote.demo.service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.Event;
import com.microsoft.graph.models.OnlineMeeting;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.OnlineMeetingCollectionPage;
import com.peaknote.demo.entity.MeetingEvent;
import com.peaknote.demo.repository.MeetingEventRepository;
import com.peaknote.demo.repository.UserRepository;
import com.peaknote.demo.entity.TeamsUser;

@Service
public class MeetingInstanceSyncService {

    private final GraphServiceClient<?> graphClient;
    private final UserRepository userRepository;
    private final MeetingEventRepository meetingEventRepository;
    private final GraphService graphService;
    
    public MeetingInstanceSyncService(
        @org.springframework.beans.factory.annotation.Qualifier("graphClient") GraphServiceClient<?> graphClient,
        UserRepository userRepository,
        MeetingEventRepository meetingEventRepository,
        GraphService graphService
    ) {
        System.out.println("🔧 MeetingInstanceSyncService constructor called");
        this.graphClient = graphClient;
        this.userRepository = userRepository;
        this.meetingEventRepository = meetingEventRepository;
        this.graphService = graphService;
    }

    /**
     * Scheduled task: runs every Sunday at 11 PM, syncs meetings for next week (Monday to Sunday)
     * Cron expression: second minute hour day month week
     * 0 0 23 * * SUN = every Sunday at 11 PM
     */
    @Scheduled(cron = "0 0 23 * * SUN")
    public void scheduledSyncNextWeekMeetings() {
        System.out.println("🕐 Scheduled task triggered at: " + Instant.now());
        System.out.println("📅 Starting weekly meeting sync for next week...");
        
        try {
            syncNextWeekMeetings();
            System.out.println("✅ Weekly meeting sync completed successfully");
        } catch (Exception e) {
            System.err.println("❌ Weekly meeting sync failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sync meetings for next week (Monday to Sunday)
     */
    public void syncNextWeekMeetings() {
        // Get next week's start and end dates
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate nextSunday = nextMonday.plusDays(6);
        
        // Convert to UTC time to ensure database stores standard time
        LocalDateTime mondayStart = LocalDateTime.of(nextMonday, LocalTime.MIN); // Monday 00:00
        LocalDateTime sundayEnd = LocalDateTime.of(nextSunday, LocalTime.MAX);   // Sunday 23:59:59
        
        // Convert to UTC time
        ZonedDateTime mondayStartUTC = mondayStart.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime sundayEndUTC = sundayEnd.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        
        System.out.println("📅 Syncing meetings for next week:");
        System.out.println("   Monday: " + mondayStartUTC + " (UTC)");
        System.out.println("   Sunday: " + sundayEndUTC + " (UTC)");
        
        syncMeetingsInTimeRange(mondayStartUTC.toOffsetDateTime(), sundayEndUTC.toOffsetDateTime());
    }

    /**
     * Sync meetings within specified time range
     */
    public void syncMeetingsInTimeRange(OffsetDateTime start, OffsetDateTime end) {
        System.out.println("🟡 Running meeting sync for time range: " + start + " to " + end);
        List<TeamsUser> users = userRepository.findAll();

        for (TeamsUser user : users) {
            try {
                System.out.println("🔄 Syncing meetings for user: " + user.getOid());
                
                List<Event> events = graphClient
                    .users(user.getOid())
                    .calendarView()
                    .buildRequest(
                        Arrays.asList(
                            new QueryOption("startDateTime", start.toString()),
                            new QueryOption("endDateTime", end.toString())
                        )
                    )
                    .select("id,subject,start,end,onlineMeeting,isOnlineMeeting")
                    .top(50)
                    .get()
                    .getCurrentPage();

                System.out.println("📅 Found " + events.size() + " events for user " + user.getOid());

                for (Event event : events) {
                    System.out.println("📦 Processing event: " + event.id + " - " + event.subject);
                    
                    if (!event.isOnlineMeeting) {
                        System.out.println("❌ Skipping event " + event.id + " (no onlineMeeting)");
                        continue;
                    }

                    // Check if event already exists
                    if (meetingEventRepository.existsById(event.id)) {
                        System.out.println("⏭️ Event " + event.id + " already exists, skipping...");
                        continue;
                    }

                    try {
                        // Parse datetime and convert to UTC for database storage
                        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                            .toFormatter()
                            .withZone(ZoneOffset.UTC);

                        // Get the real meetingID using GraphService
                        String meetingId = null;
                        if (event.onlineMeeting != null && event.onlineMeeting.joinUrl != null) {
                            try {
                                System.out.println("🔍 Querying real meetingID for joinUrl: " + event.onlineMeeting.joinUrl);
                                OnlineMeetingCollectionPage meetings = graphService.getOnlineMeetingsByJoinUrl(user.getOid(), event.onlineMeeting.joinUrl);
                                
                                if (meetings != null && !meetings.getCurrentPage().isEmpty()) {
                                    OnlineMeeting meeting = meetings.getCurrentPage().get(0);
                                    meetingId = meeting.id;
                                    System.out.println("✅ Found real meetingID: " + meetingId);
                                } else {
                                    System.out.println("⚠️ No meeting found for joinUrl, using joinUrl as fallback");
                                    meetingId = event.onlineMeeting.joinUrl;
                                }
                            } catch (Exception e) {
                                System.err.println("❌ Failed to get real meetingID: " + e.getMessage());
                                System.out.println("⚠️ Using joinUrl as fallback for meetingID");
                                meetingId = event.onlineMeeting.joinUrl;
                            }
                        }

                        // Create MeetingEvent entity with UTC timestamps
                        MeetingEvent meetingEvent = new MeetingEvent();
                        meetingEvent.setEventId(event.id);                    // Graph API event ID
                        meetingEvent.setUserId(user.getOid());               // Creator ID
                        meetingEvent.setSubject(event.subject != null ? event.subject : "No Subject");
                        
                        // Parse time and convert to UTC for storage
                        Instant startTimeUTC = Instant.from(formatter.parse(event.start.dateTime));
                        Instant endTimeUTC = Instant.from(formatter.parse(event.end.dateTime));
                        
                        meetingEvent.setStartTime(startTimeUTC);
                        meetingEvent.setEndTime(endTimeUTC);
                        meetingEvent.setJoinUrl(event.onlineMeeting.joinUrl);
                        meetingEvent.setMeetingId(meetingId);               // Use real meetingID
                        meetingEvent.setTranscriptStatus(TranscriptStatus.NONE.name());
                        meetingEvent.setLastNotifiedAt(Instant.now());

                        // Save the meeting event
                        MeetingEvent savedEvent = meetingEventRepository.save(meetingEvent);
                        System.out.println("✅ Saved meeting event: " + savedEvent.getEventId() + 
                                         " with meetingID: " + savedEvent.getMeetingId() +
                                         " at " + savedEvent.getStartTime() + " (UTC)");

                    } catch (Exception e) {
                        System.err.println("❌ Failed to process event " + event.id + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                System.err.println("❌ Failed to sync for user: " + user.getOid());
                ex.printStackTrace();
            }
        }
        
        System.out.println("✅ Meeting sync completed for time range: " + start + " to " + end);
    }

    /**
     * Manual sync method (kept for compatibility with existing code)
     */
    public void syncFutureMeetings() {
        System.out.println("🟡 Manual sync triggered - syncing next 7 days...");
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusDays(7);
        syncMeetingsInTimeRange(start, end);
    }
}
