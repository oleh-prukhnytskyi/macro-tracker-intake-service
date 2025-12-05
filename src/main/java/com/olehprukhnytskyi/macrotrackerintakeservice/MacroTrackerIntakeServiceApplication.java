package com.olehprukhnytskyi.macrotrackerintakeservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@OpenAPIDefinition(
        info = @Info(
                title = "Intake Service API",
                version = "1.0",
                description = "Microservice for managing user food intake records "
                        + "and nutrition tracking"
        )
)
@EnableJpaRepositories(basePackages = {
        "com.olehprukhnytskyi.repository.jpa",
        "com.olehprukhnytskyi.macrotrackerintakeservice.repository.jpa"
})
@EntityScan(basePackages = {
        "com.olehprukhnytskyi.macrotrackerintakeservice.model",
        "com.olehprukhnytskyi.model"
})
@EnableRetry
@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class MacroTrackerIntakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerIntakeServiceApplication.class, args);
    }

}
