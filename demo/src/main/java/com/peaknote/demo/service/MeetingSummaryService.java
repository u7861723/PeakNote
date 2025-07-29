package com.peaknote.demo.service;

import java.time.Instant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class MeetingSummaryService {

    private final ChatClient chatClient;

    public MeetingSummaryService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateSummary(Instant startTime,String transcriptContent) {
      String prompt = """
      You are an AI meeting assistant. Please analyze and summarize the following sprint daily stand-up meeting transcript (in .vtt format) into structured and professional meeting minutes.

      The transcript contains timestamps and speaker names.

      Your output should include the following sections:

      1. **Meeting Title**:
         - Generate a concise and relevant title that reflects the main focus or highlight of the meeting based on the content.

      2. **Meeting Time**:
         - Just convert this time to Australia/Canberra timezone.""" + startTime.toString() + """

      3. **Participants**:
         - List all distinct speaker names mentioned in the transcript.

      4. **Team Members’ Updates**:
         For each speaker, summarize:
         - What they did yesterday
         - What they plan to do today
         - Any blockers or issues they raised

      5. **Team-wide Discussion Points**:
         - Summarize discussions involving multiple people
         - Highlight technical decisions, process changes, or coordination efforts

      6. **Action Items**:
         - List concrete tasks or follow-ups, along with responsible persons if specified

      Please format your response clearly using bullet points or numbered lists.

      Transcript:
      """ + transcriptContent;




        // Spring AI 1.0.0 新写法，链式
        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}
