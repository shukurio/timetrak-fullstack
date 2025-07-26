package com.timetrak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class TimeTrakApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeTrakApplication.class, args);
    }

}
