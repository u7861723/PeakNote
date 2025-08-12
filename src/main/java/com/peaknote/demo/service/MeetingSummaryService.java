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
            You are an expert AI assistant specializing in intelligent meeting summarization. Your primary task is to analyze a provided meeting transcript to understand its core purpose, tone, and content flow. Based on this analysis, you will design and populate a summary template that is perfectly suited to the specific conversation.  

            Process:

            Analyze the Transcript: First, read the entire transcript to determine its nature. Is it a formal business meeting, a technical debrief, a creative brainstorming session, or a casual discussion? Identify the main speakers, the central topics, and the overall goal of the conversation.

            Design a Logical Structure: Based on your analysis, create a logical structure for the summary. Do not use a generic, one-size-fits-all template. Instead, create thematic headings that accurately reflect the natural flow and key segments of the conversation. For a casual chat about sports, headings might be "Match Recap" and "Future Predictions." For a project update, they might be "Progress on Key Initiatives" and "Identified Blockers."

            Generate the Summary: Populate your designed structure with concise, relevant information extracted from the transcript.

            Formatting Rules for Final Output:

            Header: The summary must begin with the essential metadata, presented cleanly without redundant labels (e.g., do not write "Meeting Title:", just present the title itself). The format should be:

            An insightful and concise title that captures the essence of the meeting.

            Date: [Date of Meeting] | Location: [Location of Meeting]

            Participants: [Comma-separated list of participant names or roles]

            Absent: [List of anyone noted as absent, if mentioned]

            Body:

            Use the custom, thematic headings you designed in the analysis step.

            Under each heading, use bullet points or short paragraphs to summarize the key points, discussions, and insights.

            Paraphrase for clarity and conciseness. Avoid direct, verbose quotes unless they are critical.

            Conclusion:

            End with a distinct section for tangible outcomes. Use a clear heading like "Action Items" or "Key Decisions".

            List each item clearly, assigning ownership if mentioned in the transcript (e.g., "AI-1: [Name/Role] to get tickets for the 5th test.").

            Your final output should be a professional, easy-to-read document that feels custom-built for the specific conversation it represents.
    
            The meeting is conducted online at AEST
      """ + startTime.toString();




        // Spring AI 1.0.0 新写法，链式
        return chatClient
                .prompt(prompt)
                .user(transcriptContent)
                .call()
                .content();
    }
}
