package com.example.distributor;

import com.example.Constant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Bean
  Queue requestQueue() {
    return new Queue(Constant.REQUEST_QUEUE_NAME);
  }

  /** 设置交换机 */
  @Bean
  DirectExchange exchange() {
    return new DirectExchange(Constant.EXCHANGE_NAME);
  }

  @Bean
  Binding requestBinding() {

    return BindingBuilder.bind(requestQueue()).to(exchange()).with(requestQueue().getName());
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setUserCorrelationId(true);
    return rabbitTemplate;
  }
}
