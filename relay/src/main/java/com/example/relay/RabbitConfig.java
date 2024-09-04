package com.example.relay;

import com.example.Constant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Bean
  Queue requestQueue() {
    return new Queue(Constant.REQUEST_QUEUE_NAME);
  }

  /**
   * Create temporary queue for receive response
   *
   * @return temp {@link Queue}
   */
  @Bean("repliesQueue")
  Queue repliesQueue() {
    return new AnonymousQueue();
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(Constant.EXCHANGE_NAME);
  }

  @Bean
  Binding requestBinding() {
    return BindingBuilder.bind(requestQueue()).to(exchange()).with(requestQueue().getName());
  }

  @Bean
  Binding repliesBinding() {
    return BindingBuilder.bind(repliesQueue()).to(exchange()).with(repliesQueue().getName());
  }

  /** 使用 RabbitTemplate发送和接收消息 并设置回调队列地址 */
  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setReplyTimeout(30000L);
    template.setExchange(exchange().getName());
    template.setReplyAddress(repliesQueue().getName());
    template.setUserCorrelationId(true);
    template.setUseDirectReplyToContainer(true);
    return template;
  }

  @Bean
  AsyncRabbitTemplate asyncRabbitTemplate(ConnectionFactory connectionFactory) {
    return new AsyncRabbitTemplate(rabbitTemplate(connectionFactory));
  }

  /** 给返回队列设置监听器 */
  @Bean
  SimpleMessageListenerContainer replyContainer(ConnectionFactory connectionFactory) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(repliesQueue().getName());
    container.setPrefetchCount(10);
    container.setMessageListener(rabbitTemplate(connectionFactory));
    return container;
  }
}
