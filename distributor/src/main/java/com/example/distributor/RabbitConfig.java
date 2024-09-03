package com.example.distributor;

import com.example.Constant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  /** 设置消息发送RPC队列 */
  @Bean
  Queue requestQueue() {
    return new Queue(Constant.REQUEST_QUEUE_NAME);
  }

  /** 设置返回队列 */
  @Bean
  Queue repliesQueue() {
    return new Queue(Constant.REPLIES_QUEUE_NAME);
  }

  /** 设置交换机 */
  @Bean
  DirectExchange exchange() {
    return new DirectExchange(Constant.EXCHANGE_NAME);
  }

  @Bean
  FanoutExchange fanoutExchange() {
    return new FanoutExchange(Constant.FAN_OUT_EXCHANGE);
  }

  /** 请求队列和交换器绑定 */
  @Bean
  Binding requestBinding() {
    return BindingBuilder.bind(requestQueue()).to(exchange()).with(Constant.REQUEST_QUEUE_NAME);
  }

  /** 返回队列和交换器绑定 */
  @Bean
  Binding repliesBinding() {
    return BindingBuilder.bind(repliesQueue()).to(fanoutExchange());
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setUserCorrelationId(true);
    return rabbitTemplate;
  }
}
