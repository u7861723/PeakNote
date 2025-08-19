// package com.peaknote.demo.service;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.microsoft.graph.requests.GraphServiceClient;

// import okhttp3.Request;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;

// import java.time.Instant;
// import java.time.temporal.ChronoUnit;

// @Service
// public class CallRecordSyncScheduler {

//     private final GraphServiceClient<Request> graphClient;
//     private final ObjectMapper objectMapper = new ObjectMapper();

//     public CallRecordSyncScheduler(GraphServiceClient<Request> graphClient) {
//         this.graphClient = graphClient;
//     }

    //     @Scheduled(fixedRate = 600000) // Execute every 10 minutes
// public void syncCallRecords() {
//     try {
//         System.out.println("🚀 Starting to sync call records...");

//         String startTime = Instant.now().minus(29, ChronoUnit.DAYS).toString();
//         // String url = "/communications/callRecords?$filter=startDateTime ge " + startTime;
//         String url = "/communications/callRecords";
//         // Note: return may be null, need to check
//         String jsonResponse = graphClient
//                 .customRequest(url, String.class)
//                 .buildRequest()
//                 .get();

//         if (jsonResponse == null || jsonResponse.isEmpty()) {
//     System.out.println("⚠️ Response is empty, printing confirmation jsonResponse = " + jsonResponse);
//     return;
// }
// System.out.println("📄 Original JSON response: " + jsonResponse);

// JsonNode root = objectMapper.readTree(jsonResponse);
// if (!root.has("@odata.context")) {
//     System.out.println("⚠️ Response has no @odata.context, request may be abnormal or not Graph return");
//     return;
// }

// JsonNode values = root.get("value");
// if (values != null && values.isArray() && values.size() > 0) {
//     for (JsonNode record : values) {
//         String id = record.get("id").asText();
//         String start = record.get("startDateTime").asText();
//         String end = record.get("endDateTime").asText();
//         System.out.println("✅ CallRecord ID: " + id + " - Start: " + start + " - End: " + end);
//     }
// } else {
//     System.out.println("⚠️ Request is normal, but no call record data");
// }


//     } catch (Exception e) {
//         System.err.println("❌ Error syncing call records: " + e.getMessage());
//         e.printStackTrace();
//     }
// }

// }
