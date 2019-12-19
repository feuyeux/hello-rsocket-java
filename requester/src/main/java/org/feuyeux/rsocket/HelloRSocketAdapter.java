package org.feuyeux.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.pojo.HelloRequest;
import org.feuyeux.rsocket.pojo.HelloRequests;
import org.feuyeux.rsocket.pojo.HelloResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author feuyeux@gmail.com
 */
@Slf4j
@Component
public class HelloRSocketAdapter {
    private final RSocketRequester rSocketRequester;

    public HelloRSocketAdapter(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }

    /**
     * TODO METADATA_PUSH
     *
     * @param securityToken
     * @param mimeType
     * @return
     */
    public Mono<Void> metaData(String securityToken, MimeType mimeType) {
        //return rSocketRequester
        //    .route("hello-metadata")
        //    .metadata(securityToken, mimeType).?
        return Mono.empty();
    }

    /**
     * REQUEST_FNF
     * -->!
     *
     * @param id
     * @return
     */
    public Mono<Void> fireAndForget(String id) {
        return rSocketRequester
                .route("hello-forget")
                .data(new HelloRequest(id))
                .send();
    }

    /**
     * REQUEST_RESPONSE
     * request --> <-- response
     *
     * @param id
     * @return
     */
    public Mono<HelloResponse> getHello(String id) {
        return rSocketRequester
                .route("hello")
                .data(new HelloRequest(id))
                .retrieveMono(HelloResponse.class)
                .doOnNext(response -> log.info("<< [Request-Response] response id:{},value:{}",
                        response.getId(), response.getValue()));
    }

    /**
     * REQUEST_STREAM
     * request --> <-- <-- stream
     *
     * @param ids
     * @return
     */
    public Flux<HelloResponse> getHellos(List<String> ids) {
        return rSocketRequester
                .route("hello-stream")
                .data(new HelloRequests(ids))
                .retrieveFlux(HelloResponse.class)
                .doOnNext(response -> log.info("<< [Request-Stream] response id:{},value:{}",
                        response.getId(), response.getValue()));
    }

    /**
     * REQUEST_CHANNEL
     * request channel --> --> <-- --> <--
     *
     * @param helloRequestFlux
     * @return
     */
    public Flux<List<HelloResponse>> getHelloChannel(Flux<HelloRequests> helloRequestFlux) {
        return rSocketRequester
                .route("hello-channel")
                .data(helloRequestFlux, HelloRequest.class)
                .retrieveFlux(new ParameterizedTypeReference<List<HelloResponse>>() {
                })
                .doOnNext(responses -> responses.forEach(
                        response -> log.info("<< [Request-Channel] response id:{},value:{}",
                                response.getId(), response.getValue()
                        )
                ));
    }
}
