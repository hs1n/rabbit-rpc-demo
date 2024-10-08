package com.example.constant;

public class Constant {
  public static final String EXCHANGE_NAME = "rpc";

  public static final String REQUEST_QUEUE_NAME = "rpc.requests";
  public static final String REQUEST_QUEUE_ROUTING_KEY = "rpc.requests.routing";

  public static final String RABBIT_CUSTOM_MESSAGE_EXPIRATION_KEY = "rabbit.custom.message.expiration";
  public static final String MESSAGE_DEFAULT_EXPIRATION_MILLIS = "3000";
}
