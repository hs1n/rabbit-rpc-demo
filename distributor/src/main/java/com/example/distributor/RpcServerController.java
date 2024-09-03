package com.example.distributor;

import com.example.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.utils.SerializationUtils;
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


    Message response = getMessage(msg);

    Queue queue = new Queue(response.getMessageProperties().getReplyTo(), false);
    rabbitTemplate.setReplyAddress(queue.getName());
    rabbitTemplate.send(Constant.EXCHANGE_NAME, response.getMessageProperties().getReplyTo(), response);
  }

  private static Message getMessage(Message msg) {
    return new Message(
        SerializationUtils.serialize(
            "i'm receive:" + SerializationUtils.deserialize(msg.getBody())),
        getMessageProperties(msg));
  }

  private static MessageProperties getMessageProperties(Message msg) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setCorrelationId(msg.getMessageProperties().getCorrelationId());
    messageProperties.setReplyTo(msg.getMessageProperties().getReplyTo());
    messageProperties.setExpiration("3000");
    return messageProperties;
  }
}
