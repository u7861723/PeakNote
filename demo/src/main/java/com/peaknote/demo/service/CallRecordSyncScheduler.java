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

//     @Scheduled(fixedRate = 600000) // 每 10 分钟执行
// public void syncCallRecords() {
//     try {
//         System.out.println("🚀 开始同步 call records...");

//         String startTime = Instant.now().minus(29, ChronoUnit.DAYS).toString();
//         // String url = "/communications/callRecords?$filter=startDateTime ge " + startTime;
//         String url = "/communications/callRecords";
//         // 注意这里返回可能为 null，要检查
//         String jsonResponse = graphClient
//                 .customRequest(url, String.class)
//                 .buildRequest()
//                 .get();

//         if (jsonResponse == null || jsonResponse.isEmpty()) {
//     System.out.println("⚠️ 响应为空，打印确认 jsonResponse = " + jsonResponse);
//     return;
// }
// System.out.println("📄 原始 JSON 响应: " + jsonResponse);

// JsonNode root = objectMapper.readTree(jsonResponse);
// if (!root.has("@odata.context")) {
//     System.out.println("⚠️ 响应无 @odata.context，可能请求异常或非 Graph 返回");
//     return;
// }

// JsonNode values = root.get("value");
// if (values != null && values.isArray() && values.size() > 0) {
//     for (JsonNode record : values) {
//         String id = record.get("id").asText();
//         String start = record.get("startDateTime").asText();
//         String end = record.get("endDateTime").asText();
//         System.out.println("✅ CallRecord ID: " + id + " - 开始: " + start + " - 结束: " + end);
//     }
// } else {
//     System.out.println("⚠️ 请求正常，但没有 call record 数据");
// }


//     } catch (Exception e) {
//         System.err.println("❌ 同步 call records 出错: " + e.getMessage());
//         e.printStackTrace();
//     }
// }

// }
