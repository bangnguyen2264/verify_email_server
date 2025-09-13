package com.example.email_verify;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class MailService {
    private final RestTemplate restTemplate;
    private final MailProperties mailProperties;

    public void sendEmail(String to, String subject, String htmlBody) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mailProperties.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("from", "G-Weather Forecast <onboarding@resend.dev>");
        body.put("to", List.of(to));
        body.put("subject", subject);
        body.put("html", htmlBody);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        System.out.println("Resend response: " + response.getBody());
    }

    public void sendExampleMail() throws MessagingException {
        sendEmail(
                "ngtbang2264.dev@gmail.com", // chỉ được gửi về email của bạn khi test với onboarding@resend.dev
                "Test email",
                "<h3>Hello</h3><p>This is a test email using Resend API 🚀</p>"
        );
    }
}
