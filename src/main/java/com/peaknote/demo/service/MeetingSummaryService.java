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

            IMPORTANT: You must return the summary in HTML format with proper HTML tags and styling. Use semantic HTML elements and include CSS styling for a professional appearance.

            HTML Structure:
            - Use <!DOCTYPE html> declaration
            - Include <html>, <head>, and <body> tags
            - Add <meta> tags for proper encoding and viewport
            - Include embedded CSS in <style> tag for styling
            - Use semantic HTML elements like <header>, <main>, <section>, <article>, <footer>

            Header Section:
            - Use <h1> for the main title
            - Use <div> with appropriate classes for metadata (date, location, participants)
            - Style the header with a professional appearance

            Body Section:
            - Use <h2> or <h3> for thematic headings
            - Use <ul> and <li> for bullet points
            - Use <p> for paragraphs
            - Apply consistent spacing and typography

            Conclusion Section:
            - Use <h2> for the "Action Items" or "Key Decisions" heading
            - Use <ul> and <li> for action items
            - Highlight important information with <strong> or <em> tags

            CSS Styling:
            - Use a clean, professional color scheme
            - Apply consistent typography (font-family, font-size, line-height)
            - Add proper spacing between sections
            - Use subtle borders or backgrounds to separate sections
            - Ensure good readability with appropriate contrast

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
