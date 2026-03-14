package com.eip.referencedata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReferenceDataServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReferenceDataServiceApplication.class, args);
    }
}
