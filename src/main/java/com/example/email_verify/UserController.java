package com.example.email_verify;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/verify")
public class UserController {
    private final UserService userService;

    @PostMapping("/send-activation")
    public String sendActivationEmail(@RequestParam String email, @RequestParam String location) throws MessagingException {
        userService.sendActivationEmail(email,location);
        return "Email kích hoạt đã được gửi tới " + email;
    }
    @PostMapping("/unsubscribe")
    public ResponseEntity unsubscribeEmail(@RequestParam String email) throws MessagingException {
        userService.unSubscribetionEmail(email);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/activate")
    public ResponseEntity activateAccount(@RequestParam String token) {
        return ResponseEntity.ok(userService.activateAccount(token));
    }
}
