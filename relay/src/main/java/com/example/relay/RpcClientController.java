package com.example.relay;

import com.example.Constant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RpcClientController {

  private final RabbitTemplate rabbitTemplate;
  private final Environment environment;

  @Autowired
  public RpcClientController(RabbitTemplate rabbitTemplate, Environment environment) {
    this.rabbitTemplate = rabbitTemplate;
    this.environment = environment;
  }

  @GetMapping("/send")
  public Object send(String message) {
    // 创建消息对象
    String correlationId = UUID.randomUUID().toString();
    Message requestMessage = buildMessage(correlationId, message);

    log.info("client send：{}", requestMessage);

    // 客户端发送消息

    Message responseMessage =
        rabbitTemplate.sendAndReceive(
            Constant.EXCHANGE_NAME, Constant.REQUEST_QUEUE_NAME, requestMessage);

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

  private Message buildMessage(String correlationId, String message) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setCorrelationId(correlationId);
    messageProperties.setExpiration(
        environment.getProperty(
            Constant.RABBIT_CUSTOM_MESSAGE_EXPIRATION_KEY,
            Constant.MESSAGE_DEFAULT_EXPIRATION_MILLIS));
    return new Message(SerializationUtils.serialize(message), messageProperties);
  }
}
