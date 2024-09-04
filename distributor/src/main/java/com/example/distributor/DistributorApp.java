package com.example.distributor;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableRabbit
@EnableAsync
@SpringBootApplication
@ComponentScans(
    value = {
      @ComponentScan(basePackages = "com.example.commons"),
      @ComponentScan(basePackages = "com.example.distributor")
    })
public class DistributorApp {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(DistributorApp.class);
    app.run();
  }
}
