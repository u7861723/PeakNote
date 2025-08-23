package com.peaknote.demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.peaknote.demo.service.SubscriptionService;
import com.peaknote.demo.service.TeamsUserSyncService;
import com.peaknote.demo.service.MeetingInstanceSyncService;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


// @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
@EnableScheduling
public class DemoApplication {
	private final SubscriptionService subscriptionService;

	private final TeamsUserSyncService teamsUserSyncService;
	//private final MeetingInstanceSyncService meetingInstanceSyncService;

	// public DemoApplication(TeamsUserSyncService teamsUserSyncService,MeetingInstanceSyncService meetingInstanceSyncService) {
	// 	this.teamsUserSyncService = teamsUserSyncService;
	// 	this.meetingInstanceSyncService = meetingInstanceSyncService;
	// }
	public DemoApplication(SubscriptionService subscriptionService, TeamsUserSyncService teamsUserSyncService){
		this.teamsUserSyncService = teamsUserSyncService;
		this.subscriptionService = subscriptionService;
		//this.meetingInstanceSyncService = meetingInstanceSyncService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() throws InterruptedException {
		try {
			System.out.println("🟡 Starting user sync...");
			teamsUserSyncService.syncUsers();
			System.out.println("✅ User sync complete.");
			
			System.out.println("🟡 Deleting previous subscriptions...");
			subscriptionService.listAndDeleteAllSubscriptions();
			System.out.println("✅ Previous subscriptions deleted.");
			
			System.out.println("🟡 Registering subscriptions for all users...");
			subscriptionService.createSubscriptionsForAllUsers();
			System.out.println("✅ Subscription registration complete, waiting for Graph push notifications");
			// meetingInstanceSyncService.syncNextWeekMeetings();
			// System.out.println("✅ Meeting instance sync complete.");
		} catch (Exception e) {
			System.err.println("❌ Application startup initialization failed: " + e.getMessage());
			e.printStackTrace();
			// Log error but don't interrupt application startup
		}
	}


    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
	//  @EventListener(ApplicationReadyEvent.class)
	// public void  runAfterStartup() {
	// 	System.out.println("🟡 Starting user sync...");
	// 	teamsUserSyncService.syncUsers();
	// 	try {
	// 			meetingInstanceSyncService.syncFutureMeetings();
	// 		} catch (Exception e) {
	// 			System.err.println("❌ Failed to sync future meetings: " + e.getMessage());
	// 			e.printStackTrace();
	// 		}
	// 	System.out.println("✅ User sync complete.");
	// }
}
