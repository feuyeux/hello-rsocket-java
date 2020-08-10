## hello-rsocket-java
![](https://github.com/feuyeux/hello-rsocket/blob/master/doc/hello-rsocket.png)

### BUILD&START
#### common
```bash
▶ cd common
▶ mvn clean install
```

#### responder
```bash
▶ cd responder
▶ mvn spring-boot:run
```

#### requester
```bash
▶ cd requester
▶ mvn spring-boot:run
```

### TEST

#### FireAndForget

cli
```bash
curl http://localhost:8989/api/hello-forget
```

responder
```bash
>> [FireAndForget] FNF: JAVA
```

#### Request-Response
cli
```bash
▶ curl http://localhost:8989/api/hello/1
{"id":"1","value":"Bonjour"}
```

responder
```bash
>> [Request-Response] data: HelloRequest(id=1)
```

requester
```bash
 << [Request-Response] response id:1,value:Bonjour
```

#### Request-Stream
cli
```bash
▶ curl http://localhost:8989/api/hello-stream

data:{"id":"3","value":"こんにちは"}

data:{"id":"4","value":"Ciao"}

data:{"id":"2","value":"Hola"}

data:{"id":"3","value":"こんにちは"}

data:{"id":"3","value":"こんにちは"}
```

responder
```bash
>> [Request-Stream] data: HelloRequests(ids=[3, 4, 2, 3, 3])
```

requester
```bash
<< [Request-Stream] response id:3,value:こんにちは
<< [Request-Stream] response id:4,value:Ciao
<< [Request-Stream] response id:2,value:Hola
<< [Request-Stream] response id:3,value:こんにちは
<< [Request-Stream] response id:3,value:こんにちは
```

#### Request-Channel
cli
```bash
▶ curl http://localhost:8989/api/hello-channel
data:[{"id":"4","value":"Ciao"},{"id":"4","value":"Ciao"},{"id":"2","value":"Hola"}]

data:[{"id":"4","value":"Ciao"},{"id":"3","value":"こんにちは"},{"id":"0","value":"Hello"}]

data:[{"id":"2","value":"Hola"},{"id":"0","value":"Hello"},{"id":"2","value":"Hola"}]
```

responder
```bash
[Request-Channel] data:HelloRequests(ids=[4, 4, 2])
[Request-Channel] data:HelloRequests(ids=[4, 3, 0])
[Request-Channel] data:HelloRequests(ids=[2, 0, 2])
```

requester
```bash
<< [Request-Channel] response id:4,value:Ciao
<< [Request-Channel] response id:4,value:Ciao
<< [Request-Channel] response id:2,value:Hola

<< [Request-Channel] response id:4,value:Ciao
<< [Request-Channel] response id:3,value:こんにちは
<< [Request-Channel] response id:0,value:Hello

<< [Request-Channel] response id:2,value:Hola
<< [Request-Channel] response id:0,value:Hello
<< [Request-Channel] response id:2,value:Hola
```

### more
- rsocket security: https://github.com/feuyeux/hello-rsocket-security-java