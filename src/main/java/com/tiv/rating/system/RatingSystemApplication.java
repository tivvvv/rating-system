package com.tiv.rating.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tiv.rating.system.mapper")
public class RatingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RatingSystemApplication.class, args);
    }

}