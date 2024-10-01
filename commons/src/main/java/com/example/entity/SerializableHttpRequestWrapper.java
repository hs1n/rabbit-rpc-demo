package com.example.entity;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class SerializableHttpRequestWrapper implements Serializable {
  @Serial private static final long serialVersionUID = -3665963160311684502L;
  private String method;
  private String uri;
  private String querystring;
  private Map<String, List<String>> headers;
  private String payload;
}
