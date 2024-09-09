package com.example.distributor;

import com.example.Constant;
import com.example.MessageUtils;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.apache.fury.ThreadSafeFury;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RpcServerListener {

  private final RabbitTemplate rabbitTemplate;
  private final Environment environment;
  private final ThreadSafeFury threadSafeFury;

  @Autowired
  public RpcServerListener(
      RabbitTemplate rabbitTemplate, Environment environment, ThreadSafeFury threadSafeFury) {
    this.rabbitTemplate = rabbitTemplate;
    this.environment = environment;
    this.threadSafeFury = threadSafeFury;
  }

  @RabbitListener(
      queues = {Constant.REQUEST_QUEUE_NAME},
      ackMode = "NONE",
      concurrency = "1-100")
  public void process(Message message, @Header(AmqpHeaders.CONSUMER_QUEUE) String queue) {
    log.debug("server receives : {} on {}", message.toString(), queue);
    String correlationId = message.getMessageProperties().getCorrelationId();
    String replyTo = message.getMessageProperties().getReplyTo();

    String expiration =
        environment.getProperty(
            Constant.RABBIT_CUSTOM_MESSAGE_EXPIRATION_KEY,
            Constant.MESSAGE_DEFAULT_EXPIRATION_MILLIS);

    Message responseMessage =
        new Message(
            buildResponse(message.getBody()),
            MessageUtils.getMessageProperties(correlationId, expiration));

    rabbitTemplate.send(Constant.EXCHANGE_NAME, replyTo, responseMessage);
  }

  @Timed(
      value = "distributor.serialization",
      description = "message deserialization and deserialization")
  private byte[] buildResponse(byte[] msg) {
    //    if (threadSafeFury.deserialize(msg) instanceof SerializableHttpRequestWrapper wrapper) {
    //      return threadSafeFury.serialize(wrapper);
    //    } else {
    //      return new byte[0];
    //    }
    return msg;
  }
}
