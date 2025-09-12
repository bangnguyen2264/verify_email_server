package com.example.email_verify;

import com.example.email_verify.UserRepository;
import com.example.email_verify.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${WEATHER_API_KEY}")
    private String WEATHER_API_KEY;
    @Value("${WEB_URL}")
    private String WEB_URL;

    public void sendActivationEmail(String email, String location) throws MessagingException {
        boolean checkExist = userRepository.existsByEmail(email);
        if (checkExist) {
            throw new MessagingException("Email already exists");
        }
        String token = UUID.randomUUID().toString();

        Users user = new Users();
        user.setEmail(email);
        user.setActivationToken(token);
        user.setActivated(false);
        user.setLocation(location);
        userRepository.save(user);

        sendActivationEmailContent(email, token);
    }

    public void unSubscribetionEmail(String email) throws MessagingException {
        Users users = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(users);
    }

    private void sendActivationEmailContent(String email, String token) throws MessagingException {
        String activationLink = WEB_URL + "/verify?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Activate Your Account");
        helper.setText(
                "<h3>Hello!</h3>" +
                        "<p>Please click the link below to activate your account:</p>" +
                        "<a href='" + activationLink + "'>Activate Account</a>",
                true
        );

        mailSender.send(message);
    }

    public boolean activateAccount(String token) {
        Users user = userRepository.findByActivationToken(token);
        if (user != null && !user.isActivated()) {
            user.setActivated(true);
            user.setActivationToken(null);
            userRepository.save(user);
            return true;
        }
        throw new IllegalArgumentException("Invalid activation token or user was activated");
    }

    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Ho_Chi_Minh")
    public void sendWeatherEmailsAutomatically() {
        List<Users> activeUsers = userRepository.findByActivatedTrue();
        for (Users user : activeUsers) {
            try {
                if (user.getLocation() != null) {
                    sendWeatherEmail(user.getEmail(), user.getLocation());
                    System.out.println("Weather email sent to: " + user.getEmail() + " at " + new java.util.Date());
                }
            } catch (MessagingException e) {
                System.err.println("Error sending weather email to " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private void sendWeatherEmail(String email, String location) throws MessagingException {
        // Gọi WeatherAPI
        String url = "https://api.weatherapi.com/v1/forecast.json?q=" + location + "&days=1&key=" + WEATHER_API_KEY;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String weatherData = response.getBody();

        StringBuilder emailContent = new StringBuilder();

        try {
            JsonNode root = objectMapper.readTree(weatherData);

            // Thông tin địa điểm
            JsonNode loc = root.get("location");
            emailContent.append("<h2>Weather Report - ").append(loc.get("name").asText())
                    .append(", ").append(loc.get("region").asText())
                    .append(" (").append(loc.get("country").asText()).append(")")
                    .append("</h2>");
            emailContent.append("<p><b>Local time:</b> ").append(loc.get("localtime").asText()).append("</p>");

            // Thông tin thời tiết hiện tại
            JsonNode current = root.get("current");
            emailContent.append("<h3>Current Weather</h3>");
            emailContent.append("<ul>");
            emailContent.append("<li>Temperature: ").append(current.get("temp_c").asText()).append(" °C</li>");
            emailContent.append("<li>Condition: ").append(current.get("condition").get("text").asText()).append("</li>");
            emailContent.append("<li>Humidity: ").append(current.get("humidity").asText()).append("%</li>");
            emailContent.append("<li>Wind: ").append(current.get("wind_kph").asText()).append(" kph (")
                    .append(current.get("wind_dir").asText()).append(")</li>");
            emailContent.append("<li>Feels Like: ").append(current.get("feelslike_c").asText()).append(" °C</li>");
            emailContent.append("<li>UV Index: ").append(current.get("uv").asText()).append("</li>");
            emailContent.append("</ul>");


        } catch (Exception e) {
            emailContent.append("<p>Error parsing weather data: ").append(e.getMessage()).append("</p>");
        }

        // Gửi email
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Daily Weather Forecast");
        helper.setText(emailContent.toString(), true);

        mailSender.send(message);
    }

}
