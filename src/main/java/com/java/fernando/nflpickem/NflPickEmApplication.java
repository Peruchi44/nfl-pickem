package com.java.fernando.nflpickem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NflPickEmApplication {

    public static void main(String[] args) {
        SpringApplication.run(NflPickEmApplication.class, args);
    }
}