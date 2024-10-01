package com.example.commons;

import com.example.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.SimplePropertyValueConnectionNameStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CommonRabbitConfiguration {

  @Bean
  public SimplePropertyValueConnectionNameStrategy cns() {
    return new SimplePropertyValueConnectionNameStrategy("spring.application.name");
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
