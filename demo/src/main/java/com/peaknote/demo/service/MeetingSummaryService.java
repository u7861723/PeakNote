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
        String prompt = "Please summarize the meeting content into a structured meeting minutes, including the main discussion points, decisions, and tasks to be completed:\n\n" + transcriptContent;

        // Spring AI 1.0.0 新写法，链式
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
