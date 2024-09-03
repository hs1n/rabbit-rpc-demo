package com.example.distributor;

import com.example.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RpcServerController {
  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public RpcServerController(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @RabbitListener(queues = Constant.REQUEST_QUEUE_NAME)
  public void process(Message msg) {
    log.info("server receive : {}", msg.toString());

    MessageProperties messageProperties = getMessageProperties(msg);

    Message response =
        new Message(("i'm receive:" + new String(msg.getBody())).getBytes(), messageProperties);
    CorrelationData correlationData =
        new CorrelationData(messageProperties.getCorrelationId());
    log.info("correctionData: {}", correlationData);
    rabbitTemplate.send(
        Constant.EXCHANGE_NAME, Constant.REPLIES_QUEUE_NAME, response, correlationData);
  }

  private static MessageProperties getMessageProperties(Message msg) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setCorrelationId(msg.getMessageProperties().getCorrelationId());
    messageProperties.setExpiration("1000");
    return messageProperties;
  }
}
