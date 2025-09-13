package com.example.email_verify;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ResendJavaMailSender implements JavaMailSender {

    private final MailProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public MimeMessage createMimeMessage() {
        return new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        throw new UnsupportedOperationException("Not supported with Resend adapter");
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        try {
            // Subject
            String subject = mimeMessage.getSubject();

            // Recipients
            String[] toAddresses = Arrays.stream(mimeMessage.getRecipients(Message.RecipientType.TO))
                    .map(Address::toString)
                    .toArray(String[]::new);

            // Content
            String content = extractContent(mimeMessage);

            Map<String, Object> body = Map.of(
                    "from", properties.getFrom(),
                    "to", toAddresses,
                    "subject", subject,
                    "html", content // đảm bảo gửi đúng HTML
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate
                    .postForEntity("https://api.resend.com/emails", request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Resend API error: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with Resend", e);
        }
    }

    /**
     * Extract HTML or plain text content from MimeMessage
     */
    private String extractContent(MimeMessage mimeMessage) throws Exception {
        Object content = mimeMessage.getContent();

        if (content instanceof String str) {
            return str;
        }

        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);

                if (part.isMimeType("text/html")) {
                    return (String) part.getContent();
                }

                if (part.isMimeType("text/plain")) {
                    return (String) part.getContent();
                }
            }
        }

        return "";
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage msg : mimeMessages) {
            send(msg);
        }
    }

    @Override
    public void send(org.springframework.mail.SimpleMailMessage simpleMessage) throws MailException {
        throw new UnsupportedOperationException("Use MimeMessage instead");
    }

    @Override
    public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) throws MailException {
        throw new UnsupportedOperationException("Use MimeMessage instead");
    }
}
