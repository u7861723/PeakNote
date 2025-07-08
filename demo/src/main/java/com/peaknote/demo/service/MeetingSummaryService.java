package com.peaknote.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class MeetingSummaryService {

    private final ChatClient chatClient;

    public MeetingSummaryService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateSummary(String transcriptContent) {
        String prompt = "请将以下会议内容总结为一份结构化会议纪要，包含主要讨论点、决策、待办事项等：\n\n" + transcriptContent;

        // Spring AI 1.0.0 新写法，链式
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
