package com.example.relay;

import com.example.Constant;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RpcClientController {

  private final RabbitTemplate rabbitTemplate;

  @Autowired
  public RpcClientController(
      RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  @GetMapping("/send")
  public String send(String message) {
    // 创建消息对象
    Message newMessage = getMessage(message);

    log.info("client send：{}", newMessage);

    // 客户端发送消息

    Message received =
        rabbitTemplate.sendAndReceive(
            Constant.EXCHANGE_NAME, Constant.REQUEST_QUEUE_NAME, newMessage);

    String response = "";

    if (received != null) {
      // 获取已发送的消息的 correlationId
      String correlationId = newMessage.getMessageProperties().getCorrelationId();
      log.info("correlationId:{}", correlationId);

      // 获取响应头信息
      Map<String, Object> headers = received.getMessageProperties().getHeaders();
      log.info(headers.toString());

      // 获取 server 返回的消息 id
      String msgId = (String) headers.get("spring_returned_message_correlation");

      if (msgId.equals(correlationId)) {
        response = new String(received.getBody());
        log.info("client received：{}", response);
      }
    }

    return response;
  }

  private static Message getMessage(String message) {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setExpiration("1000");
    messageProperties.setReplyTo(Constant.REPLIES_QUEUE_NAME);
    String correctionId = UUID.randomUUID().toString();
    messageProperties.setCorrelationId(correctionId);
    return new Message(message.getBytes(StandardCharsets.UTF_8), messageProperties);
  }
}
