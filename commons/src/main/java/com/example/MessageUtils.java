package com.example;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;

public class MessageUtils {
  private MessageUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static MessageProperties getMessageProperties(String correlationId, String expiration) {
    MessageProperties messageProperties = new MessageProperties();

    messageProperties.setCorrelationId(correlationId);
    messageProperties.setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
    messageProperties.setReceivedDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
    messageProperties.setExpiration(expiration);
    return messageProperties;
  }
}
