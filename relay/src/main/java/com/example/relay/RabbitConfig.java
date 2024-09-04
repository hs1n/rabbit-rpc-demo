package com.example.relay;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  /**
   * Create temporary queue for receive response
   *
   * @return temp {@link Queue}
   */
  @Bean("repliesQueue")
  Queue repliesQueue() {
    return new AnonymousQueue(new Base64UrlNamingStrategy("relay-"));
  }

  @Bean
  Binding repliesBinding(DirectExchange exchange) {
    return BindingBuilder.bind(repliesQueue()).to(exchange).with(repliesQueue().getName());
  }

  /** 使用 RabbitTemplate发送和接收消息 并设置回调队列地址 */
  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, DirectExchange exchange) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setReplyTimeout(30000L);
    template.setExchange(exchange.getName());
    template.setReplyAddress(repliesQueue().getName());
    template.setUserCorrelationId(true);
    template.setUseDirectReplyToContainer(true);
    return template;
  }

  @Bean
  AsyncRabbitTemplate asyncRabbitTemplate(
      ConnectionFactory connectionFactory, DirectExchange exchange) {
    return new AsyncRabbitTemplate(rabbitTemplate(connectionFactory, exchange));
  }

  /** 给返回队列设置监听器 */
  @Bean
  SimpleMessageListenerContainer replyContainer(
      ConnectionFactory connectionFactory, DirectExchange exchange) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(repliesQueue().getName());
    container.setMessageListener(rabbitTemplate(connectionFactory, exchange));
    return container;
  }
}
