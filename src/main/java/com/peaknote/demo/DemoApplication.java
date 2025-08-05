package com.peaknote.demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.peaknote.demo.service.MeetingInstanceSyncService;
import com.peaknote.demo.service.SubscriptionService;
import com.peaknote.demo.service.TeamsUserSyncService;

//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;


// @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
@EnableScheduling
public class DemoApplication {
	private final SubscriptionService subscriptionService;

	// private final TeamsUserSyncService teamsUserSyncService;
	// private final MeetingInstanceSyncService meetingInstanceSyncService;

	// public DemoApplication(TeamsUserSyncService teamsUserSyncService,MeetingInstanceSyncService meetingInstanceSyncService) {
	// 	this.teamsUserSyncService = teamsUserSyncService;
	// 	this.meetingInstanceSyncService = meetingInstanceSyncService;
	// }
	public DemoApplication(SubscriptionService subscriptionService){
		this.subscriptionService = subscriptionService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterStartup() throws InterruptedException {
	System.out.print("整在1删除之前的订阅");
	subscriptionService.listAndDeleteAllSubscriptions();
    System.out.println("🟡 正在为所有用户注册订阅...");
    subscriptionService.createSubscriptionsForAllUsers();
    System.out.println("✅ 订阅注册完成，等待 Graph 推送通知");
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
