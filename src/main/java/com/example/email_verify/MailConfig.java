package com.example.email_verify;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
@Configuration
@RequiredArgsConstructor
public class MailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender(ResendJavaMailSender resendSender) {
        return resendSender;
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
