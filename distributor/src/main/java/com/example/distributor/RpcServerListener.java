package com.example.distributor;

import com.example.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RpcServerListener {

  private final RabbitTemplate rabbitTemplate;
  private final Environment environment;

  @Autowired
  public RpcServerListener(RabbitTemplate rabbitTemplate, Environment environment) {
    this.rabbitTemplate = rabbitTemplate;
    this.environment = environment;
  }

  @RabbitListener(queues = Constant.REQUEST_QUEUE_NAME)
  public void process(Message message) {
    log.info("server receives : {}", message.toString());
    String correlationId = message.getMessageProperties().getCorrelationId();
    String replyTo = message.getMessageProperties().getReplyTo();

    Message responseMessage =
        new Message(buildResponse(message.getBody()), getMessageProperties(correlationId, replyTo));

    rabbitTemplate.send(Constant.EXCHANGE_NAME, replyTo, responseMessage);
  }

  private byte[] buildResponse(byte[] msg) {
    return SerializationUtils.serialize("i'm receive:" + SerializationUtils.deserialize(msg));
  }

  private MessageProperties getMessageProperties(String correlationId, String replyTo) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setCorrelationId(correlationId);
    messageProperties.setReplyTo(replyTo);
    messageProperties.setExpiration(
        environment.getProperty(
            Constant.RABBIT_CUSTOM_MESSAGE_EXPIRATION_KEY,
            Constant.MESSAGE_DEFAULT_EXPIRATION_MILLIS));
    return messageProperties;
  }
}
