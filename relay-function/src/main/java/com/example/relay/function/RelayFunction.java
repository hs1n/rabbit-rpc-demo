package com.example.relay.function;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RelayFunction implements HttpFunction {

  public static final String APPLICATION_JSON = "application/json";

  // Simple function to return "Hello World"
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException, ExecutionException, InterruptedException {
    String responseBody = "";
    try (RPCClient rpcClient = new RPCClient()) {
      responseBody = rpcClient.call(getSerializableHttpRequestWrapper(request));
    } catch (TimeoutException timeoutException) {
      response.getWriter().write("{}");
    }

    response.setContentType(APPLICATION_JSON);
    response.getWriter().write(URLDecoder.decode(responseBody, StandardCharsets.UTF_8));
  }

  private static SerializableHttpRequestWrapper getSerializableHttpRequestWrapper(
      HttpRequest request) throws IOException {
    SerializableHttpRequestWrapper requestWrapper = new SerializableHttpRequestWrapper();
    requestWrapper.setUri(request.getPath());
    requestWrapper.setPayload(new String(request.getInputStream().readAllBytes()));
    requestWrapper.setMethod(request.getMethod());
    requestWrapper.setHeaders(request.getHeaders());
    Optional<String> query = request.getQuery();
    query.ifPresent(requestWrapper::setQuerystring);
    return requestWrapper;
  }
}
