package com.peaknote.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.peaknote.demo.dto.MailRequest;
import com.peaknote.demo.service.MailService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/email")
public class MailController {
    @Autowired
    private MailService mailService;

    // Method 1: Frontend sends HTML string
    @PostMapping("/forwardHtml")
    public String forwardHtml(@RequestBody MailRequest request) {
        try {
            System.out.println("Received request: " + request.toString());
            mailService.forwardHtmlAsPdf(request.getHtmlContent(), request.getRecipients());
            return "Forward successful";
        } catch (Exception e) {
            e.printStackTrace();
            return "Forward failed: " + e.getMessage();
        }
    }
}
