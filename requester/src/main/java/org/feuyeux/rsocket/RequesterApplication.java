package org.feuyeux.rsocket;

import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

/**
 * @author feuyeux@gmail.com
 */
@Slf4j
@SpringBootApplication
public class RequesterApplication {
    final String dataMimeTypeValue = MimeTypeUtils.APPLICATION_JSON_VALUE;
    final MimeType dataMimeType = MimeTypeUtils.APPLICATION_JSON;
    final String metadataMimeTypeValue = "message/x.rsocket.composite-metadata.v0";
    final MimeType metadataMimeType = MimeTypeUtils.parseMimeType(metadataMimeTypeValue);

    public static void main(String[] args) {
        SpringApplication.run(RequesterApplication.class);
    }

    @Bean
    public RSocket rSocket() {
        return RSocketFactory.connect()
            .metadataMimeType(metadataMimeTypeValue)
            .dataMimeType(dataMimeTypeValue)
            .frameDecoder(PayloadDecoder.ZERO_COPY)
            .fragment(1024)
            .transport(TcpClientTransport.create(7878))
            .start().block();
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketStrategies strategies) {
        return RSocketRequester.wrap(rSocket(),
            dataMimeType,
            metadataMimeType,
            strategies);
    }
}

