package com.example.distributor;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableRabbit
@EnableAsync
@SpringBootApplication
public class DistributorApp {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(DistributorApp.class);
    app.run();
  }
}
