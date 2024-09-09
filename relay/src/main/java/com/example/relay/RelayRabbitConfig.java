package com.example.relay;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RelayRabbitConfig {

  @Bean
  public ConnectionFactory rabbitConnectionFactory(ConnectionNameStrategy cns) {
    CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
    connectionFactory.setConnectionNameStrategy(cns);

    // 影響 relay 的 throughput
    connectionFactory.setChannelCacheSize(10);
    connectionFactory.setChannelCheckoutTimeout(3000L);
    return connectionFactory;
  }

  /**
   * Create temporary queue for receive response
   *
   * @return temp {@link Queue}
   */
  @Bean("responseQueue")
  Queue responseQueue() {
    return new AnonymousQueue(new Base64UrlNamingStrategy("response-"));
  }

  @Bean
  Binding responseBinding(DirectExchange exchange) {
    // Use queue name for routing
    return BindingBuilder.bind(responseQueue()).to(exchange).withQueueName();
  }

  @Bean
  RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, DirectExchange exchange) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setReplyTimeout(30000L);
    RetryTemplate retryTemplate = new RetryTemplate();
    retryTemplate.setRetryPolicy(new MaxAttemptsRetryPolicy());
    template.setRetryTemplate(retryTemplate);
    template.setExchange(exchange.getName());
    template.setReplyAddress(responseQueue().getName());
    template.setUserCorrelationId(true);
    template.setUseDirectReplyToContainer(true);
    return template;
  }

  @Bean
  AsyncRabbitTemplate asyncRabbitTemplate(
      ConnectionFactory connectionFactory, DirectExchange exchange) {
    return new AsyncRabbitTemplate(rabbitTemplate(connectionFactory, exchange));
  }

  @Bean
  SimpleMessageListenerContainer replyContainer(
      ConnectionFactory connectionFactory, DirectExchange exchange) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);

    container.setQueues(responseQueue());
    container.setPrefetchCount(250);
    container.setBatchSize(5);

    container.setConcurrentConsumers(5);
    container.setConsumeDelay(500L); // consume delay
    container.setReceiveTimeout(5000L); // 5 seconds
    container.setAcknowledgeMode(AcknowledgeMode.NONE);

    container.setMessageListener(rabbitTemplate(connectionFactory, exchange));
    return container;
  }
}
