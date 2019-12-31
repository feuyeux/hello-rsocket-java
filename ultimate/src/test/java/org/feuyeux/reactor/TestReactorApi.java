package org.feuyeux.reactor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TestReactorApi {
    @Test
    public void testFastProducer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux<Integer> publisher = Flux.range(2005, 20);

        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(5);
            }

            @SneakyThrows
            @Override
            public void onNext(Integer i) {
                TimeUnit.MILLISECONDS.sleep(200);
                log.info("handle {}", i);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                log.info("complete");
                latch.countDown();
            }
        };

        publisher.filter(i -> i % 2 == 0).subscribe(subscriber);
        publisher.filter(i -> i >= 2020).subscribe(subscriber);
        latch.await();
    }

    @Test
    public void testSlowProducer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Flux<Integer> publisher = Flux.range(2004, 20).delayElements(Duration.ofMillis(200));
        Subscriber<Integer> subscriber = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer i) {
                log.info("handle {}", i);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                log.info("complete");
                latch.countDown();
            }
        };

        publisher.filter(i -> i % 2 == 0)
                .subscribe(subscriber);
        latch.await();
    }
}
