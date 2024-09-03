package com.example.distributor;

import com.example.Constant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
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
  TopicExchange exchange() {
    return new TopicExchange(Constant.EXCHANGE_NAME);
  }

  /** 请求队列和交换器绑定 */
  @Bean
  Binding requestBinding() {
    return BindingBuilder.bind(requestQueue()).to(exchange()).with(Constant.ROUTING_KEY);
  }

  /** 返回队列和交换器绑定 */
  @Bean
  Binding repliesBinding() {
    return BindingBuilder.bind(repliesQueue()).to(exchange()).with(Constant.ROUTING_KEY);
  }
}
