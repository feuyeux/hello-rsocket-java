package org.feuyeux.rsocket.ultimate;

import com.alibaba.fastjson.JSON;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.exceptions.RejectedResumeException;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.pojo.HelloRequest;
import org.feuyeux.rsocket.pojo.HelloRequests;
import org.feuyeux.rsocket.pojo.HelloResponse;
import org.feuyeux.rsocket.utils.HelloUtils;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class RSocketClient {
    public static final String HOST = "localhost";
    public static final int PORT = 7878;
    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        RSocket socket = init();
        if (socket != null) {
            execMetaPush(socket);
            execFireAndForget(socket);
            execRequestResponse(socket);
            execRequestStream(socket);
            execRequestChannel(socket);
        }
    }

    private static RSocket init() {
        TcpClientTransport tcpTransport = TcpClientTransport.create(HOST, PORT);
        WebsocketClientTransport wsTransport = WebsocketClientTransport.create(HOST, PORT);

        return RSocketFactory.connect()
                .errorConsumer(throwable -> {
                    if (throwable instanceof RejectedResumeException) {
                        init();
                    }
                })
                .resume()
                .resumeSessionDuration(Duration.ofSeconds(60))
                .transport(tcpTransport)
                .start()
                .block();
    }

    public static void execMetaPush(RSocket socket) {
        log.info("====ExecMetaPush====");
        Payload payload = DefaultPayload.create(new byte[]{}, "JAVA".getBytes());
        socket.metadataPush(payload).block();
    }

    public static void execFireAndForget(RSocket socket) {
        log.info("====ExecFireAndForget====");
        HelloRequest helloRequest = new HelloRequest("1");
        Payload payload = DefaultPayload.create(JSON.toJSONString(helloRequest));
        socket.fireAndForget(payload).block();
    }

    public static void execRequestResponse(RSocket socket) throws InterruptedException {
        log.info("====ExecRequestResponse====");
        HelloRequest helloRequest = new HelloRequest("1");
        Payload payload = DefaultPayload.create(JSON.toJSONString(helloRequest));
        CountDownLatch c = new CountDownLatch(1);
        socket.requestResponse(payload)
                .doOnError(e -> {
                    log.error("", e);
                    c.countDown();
                })
                .subscribe(p -> {
                    HelloResponse response = JSON.parseObject(p.getDataUtf8(), HelloResponse.class);
                    log.info("<< [Request-Response] response id:{},value:{}", response.getId(), response.getValue());
                    c.countDown();
                });
        c.await();
    }

    public static void execRequestStream(RSocket socket) throws InterruptedException {
        log.info("====ExecRequestStream====");
        List<String> ids = HelloUtils.getRandomIds(5);
        Payload payload = DefaultPayload.create(JSON.toJSONString(new HelloRequests(ids)));
        CountDownLatch c = new CountDownLatch(5);
        socket.requestStream(payload).subscribe(p -> {
            HelloResponse response = JSON.parseObject(p.getDataUtf8(), HelloResponse.class);
            log.info("<< [Request-Stream] response id:{},value:{}", response.getId(), response.getValue());
            c.countDown();
        });
        c.await();
    }

    public static void execRequestChannel(RSocket socket) throws InterruptedException {
        log.info("====ExecRequestChannel====");
        int TIMES = 300;
        CountDownLatch c = new CountDownLatch(TIMES * 3);

        Flux<Payload> send = Flux.<Payload>create(emitter -> {
            for (int i = 1; i <= TIMES; i++) {
                List<String> ids = HelloUtils.getRandomIds(3);
                Payload payload = DefaultPayload.create(JSON.toJSONString(new HelloRequests(ids)));
                emitter.next(payload);
            }
            emitter.complete();
        }).delayElements(Duration.ofMillis(1000));

        socket.requestChannel(send).subscribe(p -> {
            HelloResponse response = JSON.parseObject(p.getDataUtf8(), HelloResponse.class);
            log.info("<< [Request-Channel] response id:{},value:{}", response.getId(), response.getValue());
            c.countDown();
        });
        c.await();
    }
}
