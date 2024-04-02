package com.example.passwordKeepr.passwordKeeprTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class PasswordKeeprTestApplication implements CommandLineRunner {

	public static void main(String[] args) {
		System.out.println("Application Starts!");
		SpringApplication.run(PasswordKeeprTestApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
	}
}

