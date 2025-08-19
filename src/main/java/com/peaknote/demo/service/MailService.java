package com.peaknote.demo.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
//import com.openhtmltopdf.PdfRendererBuilder;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void forwardHtmlAsPdf(String htmlContent, List<String> recipients) throws Exception {
        System.out.println("Starting HTML to PDF conversion request, recipient count: " + recipients.size());
        
        File pdfFile = File.createTempFile("meeting_", ".pdf");
        System.out.println("Created temporary PDF file: " + pdfFile.getAbsolutePath());
        
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            System.out.println("Starting PDF generation...");
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();
            System.out.println("PDF generated successfully, file size: " + pdfFile.length() + " bytes");
        } catch (Exception e) {
            System.err.println("PDF generation failed: " + e.getMessage());
            throw new RuntimeException("PDF generation failed", e);
        }
        // 1. HTML to PDF conversion
        // File pdfFile = PdfGenerator.htmlToPdf(htmlContent);

        // 2. Construct email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("jerry527ztx@gmail.com");
        helper.setTo(recipients.toArray(new String[0])); // Multiple recipients
        System.out.println("recipients:"+recipients);
        helper.setSubject("Meeting Minutes Forward");
        helper.setText("Meeting minutes have been converted to PDF, please see attachment.", false);

        // Attachment
        helper.addAttachment("meeting.pdf", new FileSystemResource(pdfFile));

        // 3. Send email
        System.out.println("Starting to send email...");
        try {
            mailSender.send(message);
            System.out.println("Email sent successfully! Recipients: " + recipients);
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Delete temporary file (optional)
        pdfFile.delete();
    }
}
