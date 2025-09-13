package com.example.email_verify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "resend")
public class MailProperties {
    //    private String host;
//    private int port;
//    private String username;
//    private String password;
//
//    private final Map<String, String> properties = new HashMap<>();
    private String apiKey;
    private String from;
}
