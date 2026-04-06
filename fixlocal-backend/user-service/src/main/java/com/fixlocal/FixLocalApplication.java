package com.fixlocal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fixlocal.repository")
public class FixLocalApplication {

    public static void main(String[] args) {
        SpringApplication.run(FixLocalApplication.class, args);
    }
}
