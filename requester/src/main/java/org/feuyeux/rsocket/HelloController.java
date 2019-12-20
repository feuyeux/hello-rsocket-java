package org.feuyeux.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.pojo.HelloRequests;
import org.feuyeux.rsocket.pojo.HelloResponse;
import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * @author feuyeux@gmail.com
 */
@Slf4j
@RestController
public class HelloController {
    private final HelloRSocketAdapter helloRSocketAdapter;
    private final Random random = new Random();

    HelloController(HelloRSocketAdapter helloRSocketAdapter) {
        this.helloRSocketAdapter = helloRSocketAdapter;
    }

    @GetMapping("hello-metadata")
    Mono<Void> metaData() {
        MimeType mimeType = MimeType.valueOf("message/x.rsocket.authentication.bearer.v0");
        String securityToken = "...";
        return helloRSocketAdapter.metaData(securityToken, mimeType);
    }

    @GetMapping("hello-forget")
    Mono<Void> fireAndForget() {
        return helloRSocketAdapter.fireAndForget("JAVA");
    }

    @GetMapping("/hello/{id}")
    Mono<HelloResponse> getHello(@PathVariable String id) {
        return helloRSocketAdapter.getHello(id);
    }

    @GetMapping(value = "/hello-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Publisher<HelloResponse> getHellos() {
        List<String> ids = getRandomIds(5);
        log.info("random={}", ids);
        return helloRSocketAdapter.getHellos(ids);
    }

    @GetMapping(value = "/hello-channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Publisher<List<HelloResponse>> getHelloChannel() {
        Flux<HelloRequests> map = Flux.just(
                new HelloRequests(getRandomIds(3)),
                new HelloRequests(getRandomIds(3)),
                new HelloRequests(getRandomIds(3)));
        return helloRSocketAdapter.getHelloChannel(map);
    }

    private List<String> getRandomIds(int max) {
        return IntStream.range(0, max)
                .mapToObj(i -> getRandomId())
                .collect(toList());
    }

    private String getRandomId() {
        int i = random.nextInt(5);
        return String.valueOf(i);
    }
}
