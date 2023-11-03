package com.redhat.hackathon.consumer;


import io.quarkus.logging.Log;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@ApplicationScoped
public class HTTPConsumerFor_1_1 {

    @Inject
    Vertx vertx;

    @ConsumeEvent(value="http_1.1_consumer")
    @RunOnVirtualThread
    public void consume(JsonObject jsonObject) {
        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setUseAlpn(false)
                .setTcpFastOpen(true)
                .setTcpQuickAck(true)
                .setTcpKeepAlive(true)
                .setTracingPolicy(TracingPolicy.IGNORE)
                .setProtocolVersion(HttpVersion.HTTP_1_1)
                .setEnabledSecureTransportProtocols(Set.of("TLSv1.2"))
                .setSslEngineOptions(new OpenSSLEngineOptions().setUseWorkerThread(true).setSessionCacheEnabled(false))
                .setMaxPoolSize(1));
        Supplier<HttpRequest<Buffer>> supplier = new Supplier<HttpRequest<Buffer>>() {
            @Override
            public HttpRequest<Buffer> get() {
                return webClient.request(HttpMethod.GET,
                        8081,
                        "localhost",
                        "/healthz"
                )
                        .putHeader("x-id", "1")
                        .putHeader("x-path", "/healthz");
            }
        };
        //Log.info("consume | The thread name is: " + Thread.currentThread().getName());
        runTest(supplier);

    }
    @RunOnVirtualThread
    void runTest(Supplier<HttpRequest<Buffer>> supplier) {
        //Log.info("runTest | The thread name is: " + Thread.currentThread().getName());
        Promise promise = Promise.promise();
        supplier.get().send().onComplete(promise);
        promise.future().onComplete(h -> {
            Thread.ofVirtual().start(() -> runTest(supplier));
        });

    }
}
