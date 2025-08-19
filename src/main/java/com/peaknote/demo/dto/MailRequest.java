package com.peaknote.demo.dto;

import java.util.List;

/**
 * 邮件请求DTO
 */
public class MailRequest {
    private String htmlContent;
    private List<String> recipients;
    
    // 默认构造函数
    public MailRequest() {}
    
    // 带参数构造函数
    public MailRequest(String htmlContent, List<String> recipients) {
        this.htmlContent = htmlContent;
        this.recipients = recipients;
    }
    
    // Getters and Setters
    public String getHtmlContent() { 
        return htmlContent; 
    }
    
    public void setHtmlContent(String htmlContent) { 
        this.htmlContent = htmlContent; 
    }
    
    public List<String> getRecipients() { 
        return recipients; 
    }
    
    public void setRecipients(List<String> recipients) { 
        this.recipients = recipients; 
    }
    
    @Override
    public String toString() {
        return "MailRequest{" +
                "htmlContent='" + htmlContent + '\'' +
                ", recipients=" + recipients +
                '}';
    }
}
