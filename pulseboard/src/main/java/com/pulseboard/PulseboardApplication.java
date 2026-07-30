package com.pulseboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PulseboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(PulseboardApplication.class, args);
    }

}
