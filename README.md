### RabbitMQ (Docker)
```bash
docker run --rm -it -p 15672:15672 -p 5672:5672 3.13.7-management-alpine
```

```
mvn clean compile function:run -Drun.functionTarget=com.example.relay.function.RelayFunction
```