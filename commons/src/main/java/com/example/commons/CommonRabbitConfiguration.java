package com.example.commons;

import com.example.Constant;
import com.example.SerializableHttpRequestWrapper;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.SimplePropertyValueConnectionNameStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonRabbitConfiguration {

  @Bean
  public SimplePropertyValueConnectionNameStrategy cns() {
    return new SimplePropertyValueConnectionNameStrategy("spring.application.name");
  }

  @Bean
  public ThreadSafeFury threadSafeFury() {
    LoggerFactory.useSlf4jLogging(true);
    ThreadSafeFury fury =
        Fury.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(true)
            .buildThreadSafeFury();
    fury.register(SerializableHttpRequestWrapper.class);
    return fury;
  }

  @Bean
  Queue requestQueue() {
    return new Queue(Constant.REQUEST_QUEUE_NAME);
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(Constant.EXCHANGE_NAME);
  }

  @Bean
  Binding requestBinding() {
    return BindingBuilder.bind(requestQueue())
        .to(exchange())
        .with(Constant.REQUEST_QUEUE_ROUTING_KEY);
  }
}
