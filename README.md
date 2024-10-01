### RabbitMQ (Docker)
```bash
docker run --rm -it -p 15672:15672 -p 5672:5672 3.13.7-management-alpine
```

### Cloud function (local)
```
mvn -pl relay-function clean install -am
```

#### Test

```bash
$ curl  http://127.0.0.1:8080/test?a=b -d '{"foo": "bar","nice": 123}'
```

```json
{
  "method": "POST",
  "uri": "/test",
  "querystring": "a\u003db",
  "headers": {
    "Accept": [
      "*/*"
    ],
    "Content-Length": [
      "26"
    ],
    "Content-Type": [
      "application/x-www-form-urlencoded"
    ],
    "Host": [
      "127.0.0.1:8080"
    ],
    "User-Agent": [
      "curl/8.7.1"
    ]
  },
  "payload": "{\"foo\": \"bar\",\"nice\": 123}"
}
```