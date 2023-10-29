package ru.sixez.volgait;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "ru.sixez.volgait")
public class VolgaItApplication {
	public static void main(String[] args) {
		SpringApplication.run(VolgaItApplication.class, args);
	}
}
