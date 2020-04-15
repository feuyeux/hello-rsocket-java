package org.feuyeux.rsocket;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.util.ByteBufPayload;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.pojo.HelloRequest;
import org.feuyeux.rsocket.pojo.HelloRequests;
import org.feuyeux.rsocket.pojo.HelloResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author feuyeux@gmail.com
 */
@Slf4j
@Component
public class HelloRSocketAdapter {
    public static final String MESSAGE_X_ORG_FEUYEUX_RSOCKET_META = "message/x.org.feuyeux.rsocket.meta";
    private final RSocketRequester rSocketRequester;

    public HelloRSocketAdapter(RSocketRequester rSocketRequester) {
        this.rSocketRequester = rSocketRequester;
    }

    public void metaData(String metaMessage) {
        CompositeByteBuf metadataByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        CompositeMetadataFlyweight.encodeAndAddMetadata(
                metadataByteBuf,
                ByteBufAllocator.DEFAULT,
                MESSAGE_X_ORG_FEUYEUX_RSOCKET_META,
                ByteBufAllocator.DEFAULT.buffer().writeBytes(metaMessage.getBytes()));
        rSocketRequester.rsocket()
                .metadataPush(ByteBufPayload.create(Unpooled.EMPTY_BUFFER, metadataByteBuf))
                .block();
    }

    /**
     * REQUEST_FNF -->!
     *
     * @param id hello id
     * @return void
     */
    public Mono<Void> fireAndForget(String id) {
        return rSocketRequester
                .route("hello-forget")
//                .metadata(this.credentials, this.mimeType)
                .data(new HelloRequest(id))
                .send();
    }

    /**
     * REQUEST_RESPONSE request --> <-- response
     *
     * @param id hello id
     * @return hello response
     */
    public Mono<HelloResponse> getHello(String id) {
        return rSocketRequester
                .route("hello-response")
//                .metadata(this.credentials, this.mimeType)
                .data(new HelloRequest(id))
                .retrieveMono(HelloResponse.class)
                .doOnNext(response -> log.info("<< [Request-Response] response id:{},value:{}",
                        response.getId(), response.getValue()));
    }

    /**
     * REQUEST_STREAM request --> <-- <-- stream
     *
     * @param ids hello id[]
     * @return hello response flux
     */
    public Flux<HelloResponse> getHellos(List<String> ids) {
        return rSocketRequester
                .route("hello-stream")
//                .metadata(this.credentials, this.mimeType)
                .data(new HelloRequests(ids))
                .retrieveFlux(HelloResponse.class)
                .doOnNext(response -> log.info("<< [Request-Stream] response id:{},value:{}",
                        response.getId(), response.getValue()));
    }

    /**
     * REQUEST_CHANNEL request channel --> --> <-- --> <--
     *
     * @param helloRequestFlux hello request flux
     * @return hello response flux
     */
    public Flux<List<HelloResponse>> getHelloChannel(Flux<HelloRequests> helloRequestFlux) {
        return rSocketRequester
                .route("hello-channel")
//                .metadata(this.credentials, this.mimeType)
                .data(helloRequestFlux, HelloRequest.class)
                .retrieveFlux(new ParameterizedTypeReference<List<HelloResponse>>() {
                }).limitRequest(2)
                .doOnNext(responses -> responses.forEach(
                        response -> log.info("<< [Request-Channel] response id:{},value:{}",
                                response.getId(), response.getValue()
                        )
                ));
    }
}
