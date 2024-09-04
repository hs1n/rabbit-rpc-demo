package com.example.relay;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRabbit
@EnableAsync
@SpringBootApplication
@EnableScheduling
@ComponentScans(
    value = {
      @ComponentScan(basePackages = "com.example.commons"),
      @ComponentScan(basePackages = "com.example.relay")
    })
public class RelayApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RelayApp.class);
        app.run();
    }
}