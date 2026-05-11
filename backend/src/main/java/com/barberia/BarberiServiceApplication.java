package com.barberia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class BarberiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarberiServiceApplication.class, args);
    }

}
