package com.ghlove.donation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DonationApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonationApplication.class, args);
    }
}
