package com.akcardetector.ak_car_detector_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.akcardetector")
@EntityScan(basePackages = "com.akcardetector.entity")
@EnableJpaRepositories(basePackages = "com.akcardetector.repository")
public class AkCarDetectorBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AkCarDetectorBackendApplication.class, args);
    }

}