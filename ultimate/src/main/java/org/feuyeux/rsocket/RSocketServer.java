package org.feuyeux.rsocket;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.rsocket.ultimate.HelloRSocket;
import reactor.core.publisher.Mono;

@Slf4j
public class RSocketServer {
    public static final String HOST = "localhost";
    public static final int PORT = 7878;

    public static void main(String[] args) throws InterruptedException {
        TcpServerTransport tcpTransport = TcpServerTransport.create(HOST, PORT);
        WebsocketServerTransport wsTransport = WebsocketServerTransport.create(HOST, PORT);

        RSocketFactory.receive()
                .acceptor(new HelloSocketAcceptor())
                .transport(tcpTransport)
                .start()
                .subscribe();
        Thread.currentThread().join();
    }

    @Slf4j
    static class HelloSocketAcceptor implements SocketAcceptor {
        @Override
        public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
            log.debug("Received connection with setup payload: [{}] and meta-data: [{}]",
                    setup.getDataUtf8(), setup.getMetadataUtf8());
            return Mono.just(new HelloRSocket());
        }
    }
}
