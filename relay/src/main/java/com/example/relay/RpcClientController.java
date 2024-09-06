package com.example.relay;

import com.example.Constant;
import com.example.SerializableHttpRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @RequestMapping(
      value = "/**",
      method = {RequestMethod.GET, RequestMethod.POST},
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> rpc(
      @RequestHeader HttpHeaders headers,
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestBody(required = false) String requestBody) {

    String correlationId = UUID.randomUUID().toString();

    Message requestMessage =
        buildMessage(
            correlationId, new SerializableHttpRequestWrapper(request, headers, requestBody));

    log.debug("client send: {} (before template)", requestMessage);

    Message responseMessage =
        rabbitTemplate.sendAndReceive(
            Constant.EXCHANGE_NAME, Constant.REQUEST_QUEUE_NAME, requestMessage);

    log.debug("client response: {}", responseMessage);


    if (responseMessage != null) {
      String responseCorrelationId = responseMessage.getMessageProperties().getCorrelationId();
      if (correlationId.equals(responseCorrelationId)) {
        return ResponseEntity.ok(SerializationUtils.deserialize(responseMessage.getBody()));
      } else {
        return ResponseEntity.notFound().build();
      }
    } else {
      log.warn("response is null on id: {}", correlationId);
    }

    return ResponseEntity.internalServerError().build();
  }

  private Message buildMessage(
      String correlationId, SerializableHttpRequestWrapper requestWrapper) {
    MessageProperties messageProperties = new MessageProperties();

    messageProperties.setCorrelationId(correlationId);
    messageProperties.setExpiration(
        environment.getProperty(
            Constant.RABBIT_CUSTOM_MESSAGE_EXPIRATION_KEY,
            Constant.MESSAGE_DEFAULT_EXPIRATION_MILLIS));

    return new Message(SerializationUtils.serialize(requestWrapper), messageProperties);
  }
}
