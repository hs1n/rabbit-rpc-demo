package com.example;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
public class SerializableHttpRequestWrapper implements Serializable {
  @Serial private static final long serialVersionUID = -3665963160311684502L;
  private String method;
  private String uri;
  private String querystring;
  private Map<String, Object> headers;
  private String payload;



  public SerializableHttpRequestWrapper(HttpServletRequest request, HttpHeaders headers, String payload) {
    this.method = request.getMethod();
    this.uri = request.getRequestURI();
    this.querystring = request.getQueryString();
    this.headers = Collections.unmodifiableMap(headers);
    this.payload = payload;
  }
}
