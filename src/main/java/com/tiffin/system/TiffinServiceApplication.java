package com.tiffin.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TiffinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiffinServiceApplication.class, args);
    }
}
