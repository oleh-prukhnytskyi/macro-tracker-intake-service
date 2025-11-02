package com.olehprukhnytskyi.macrotrackerintakeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class MacroTrackerIntakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerIntakeServiceApplication.class, args);
    }

}
