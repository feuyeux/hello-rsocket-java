package org.feuyeux.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.pojo.HelloRequest;
import org.feuyeux.rsocket.pojo.HelloRequests;
import org.feuyeux.rsocket.pojo.HelloResponse;
import org.feuyeux.rsocket.utils.HelloUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author feuyeux@gmail.com
 */
@Slf4j
@SpringBootApplication
public class RequesterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RequesterApplication.class);
    }


    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder
                .connectTcp("localhost", 7878)
                .block();
    }

    @Bean
    ApplicationListener<ApplicationReadyEvent> ready(RSocketRequester rSocketRequester) {
        Flux<HelloRequests> helloRequestsFlux = Flux.just(
                new HelloRequests(HelloUtils.getRandomIds(3)),
                new HelloRequests(HelloUtils.getRandomIds(3)),
                new HelloRequests(HelloUtils.getRandomIds(3)));

        return event -> rSocketRequester
                .route("hello-channel")
                .data(helloRequestsFlux, HelloRequest.class)
                .retrieveFlux(new ParameterizedTypeReference<List<HelloResponse>>() {
                })
                .subscribe(rs -> rs.forEach(r ->
                        log.info("<< [Request-Channel] id:{},value:{}", r.getId(), r.getValue()
                        )
                ));
    }
}

