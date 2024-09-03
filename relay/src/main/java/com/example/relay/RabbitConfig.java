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

  /** 使用 RabbitTemplate发送和接收消息 并设置回调队列地址 */
  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setExchange(exchange().getName());
    template.setReplyAddress(repliesQueue().getName());
    template.setReplyTimeout(6000L);
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
