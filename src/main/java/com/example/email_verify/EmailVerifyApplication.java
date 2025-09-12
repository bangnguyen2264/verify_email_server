package com.example.email_verify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling

public class EmailVerifyApplication {

	public static void main(String[] args) {
		System.out.println("Hello World" + LocalDateTime.now());
		SpringApplication.run(EmailVerifyApplication.class, args);
	}

}
