package com.example.relay;

import com.example.Constant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RpcClientController {

  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public RpcClientController(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @GetMapping("/send")
  public Object send(String message) {
    // 创建消息对象
    Message requestMessage = getMessage(message);
    String correlationId = requestMessage.getMessageProperties().getCorrelationId();

    log.info("client send：{}", requestMessage);

    // 客户端发送消息

    Message responseMessage =
        rabbitTemplate.sendAndReceive(
            Constant.EXCHANGE_NAME,
            Constant.REQUEST_QUEUE_NAME,
            requestMessage,
            new CorrelationData(correlationId));

    log.info("client response: {}", responseMessage);

    Object response = "";

    if (responseMessage != null) {
      String responseCorrelationId = responseMessage.getMessageProperties().getCorrelationId();
      if (correlationId.equals(responseCorrelationId)) {
        response = SerializationUtils.deserialize(responseMessage.getBody());
      }
    }

    return response;
  }

  private Message getMessage(String message) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setExpiration("1000");
    String correctionId = UUID.randomUUID().toString();
    messageProperties.setCorrelationId(correctionId);
    return new Message(SerializationUtils.serialize(message), messageProperties);
  }
}
