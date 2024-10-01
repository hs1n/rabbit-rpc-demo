package com.example.relay.function;

import com.example.constant.Constant;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;
import org.apache.commons.lang3.SerializationUtils;

public class RPCClient implements AutoCloseable {

  private final Connection connection;
  private final Channel channel;
  private static final Gson GSON = new Gson();

  public RPCClient() throws IOException, TimeoutException {
    long startMillis = System.currentTimeMillis();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();
    channel = connection.createChannel();
    System.out.println(System.currentTimeMillis() - startMillis);
  }

  public String call(SerializableHttpRequestWrapper message)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final String corrId = UUID.randomUUID().toString();

    // queue declare
    String replyQueueName = channel.queueDeclare().getQueue();
    channel.queueBind(replyQueueName, Constant.EXCHANGE_NAME, replyQueueName);

    // async listen for response first
    final CompletableFuture<String> response = new CompletableFuture<>();

    String tag =
        channel.basicConsume(
            replyQueueName,
            true,
            (consumerTag, delivery) -> {
              if (corrId.equals(delivery.getProperties().getCorrelationId())) {
                Object deserialized = SerializationUtils.deserialize(delivery.getBody());
                response.complete(GSON.toJson(deserialized));
              }
            },
            consumerTag -> {});

    AMQP.BasicProperties props = getBasicProperties(corrId, replyQueueName);

    channel.basicPublish(
        Constant.EXCHANGE_NAME,
        "rpc.requests.routing",
        props,
        SerializationUtils.serialize(message));

    // async retrieve message
    String result = response.get(1, TimeUnit.SECONDS);
    channel.basicCancel(tag);
    return result;
  }

  private static AMQP.BasicProperties getBasicProperties(String corrId, String replyQueueName) {
    return new AMQP.BasicProperties.Builder()
        .correlationId(corrId)
        .replyTo(replyQueueName)
        .expiration("3000")
        .deliveryMode(1) // non persistent
        .build();
  }

  public void close() throws IOException {
    connection.close();
  }
}
