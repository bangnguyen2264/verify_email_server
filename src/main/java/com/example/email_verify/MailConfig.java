package com.example.email_verify;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
@Configuration
@RequiredArgsConstructor
public class MailConfig {
    private final MailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        var mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());
        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        // bind tất cả spring.mail.properties.* vào JavaMailProperties
        for (Map.Entry<String, String> entry : emailProperties.getProperties().entrySet()) {
            mailSender.getJavaMailProperties().put(entry.getKey(), entry.getValue());
        }
        // nếu muốn bật debug
        mailSender.getJavaMailProperties().put("mail.debug", "true");

        return mailSender;
    }
}
