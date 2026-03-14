package com.eip.premiumcalc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PremiumCalcServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PremiumCalcServiceApplication.class, args);
    }
}
