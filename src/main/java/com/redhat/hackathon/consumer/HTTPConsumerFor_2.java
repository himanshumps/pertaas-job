package com.redhat.hackathon.consumer;


import com.redhat.hackathon.model.HttpRequestAndBodyModel;
import com.redhat.hackathon.model.RequestModel;
import com.redhat.hackathon.supplier.HTTPRequestSupplierParameterized;
import com.redhat.hackathon.supplier.HttpRequestSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer to run HTTP/2 test
 */
public class HTTPConsumerFor_2 {

    private static AtomicInteger initCounter = new AtomicInteger(0);
    private static RateLimiter rateLimiter = null;
    @Inject
    Vertx vertx;
    @Inject
    HTTPRequestSupplierParameterized httpRequestSupplierParameterized;
    @ConfigProperty(name = "jobId")
    String jobId;
    private long endTime;

    @ConsumeEvent(value = "http_2_consumer")
    @RunOnVirtualThread // Quarkus + Java 21 feature
    public void consume(RequestModel requestModel) {
        if (initCounter.incrementAndGet() == 1) {
            rateLimiter = RateLimiter.of(jobId, RateLimiterConfig.custom()
                    .limitForPeriod(requestModel.getRequestPerSecond())
                    .limitRefreshPeriod(Duration.ofSeconds(1))
                    .timeoutDuration(Duration.ofSeconds(5))
                    .build());
        }
        // Webclient needs to be created in consume method.
        // We want to run as many connections as specified in maxConnections to ensure that we have that many connection open
        // As this is for HTTP 2.x, we ant to upgrade to HTTP/2 if the server supports it
        WebClient webClient = WebClient.create(vertx, new WebClientOptions()
                .setUseAlpn(true)
                .setTcpFastOpen(true)
                .setTcpQuickAck(true)
                .setTcpKeepAlive(true)
                .setReusePort(false)
                .setTracingPolicy(TracingPolicy.IGNORE)
                .setProtocolVersion(HttpVersion.HTTP_2)
                .setHttp2ClearTextUpgrade(true)
                .setInitialSettings(new Http2Settings()
                        .setHeaderTableSize(requestModel.getHeaderTableSize())
                        .setMaxConcurrentStreams(requestModel.getMaxConcurrentStreams())
                        .setInitialWindowSize(requestModel.getInitialWindowSize())
                        .setMaxFrameSize(requestModel.getMaxFrameSize())
                        .setMaxHeaderListSize(requestModel.getMaxHeaderListSize()))
                .setEnabledSecureTransportProtocols(Set.of("TLSv1.2"))
                .setSslEngineOptions(new OpenSSLEngineOptions().setUseWorkerThread(true).setSessionCacheEnabled(false))
                .setMaxPoolSize(1)
                .setHttp2MaxPoolSize(1)
        );
        HttpRequestSupplier httpRequestSupplier = httpRequestSupplierParameterized.getSupplier(requestModel, webClient);
        endTime = requestModel.getEndTime();
        // Run the loop for max concurrent streams to use all of them
        for (int i = 0; i < requestModel.getMaxConcurrentStreams(); i++) {
            runTest(webClient, httpRequestSupplier);
        }
    }

    void runTest(WebClient webClient, HttpRequestSupplier httpRequestSupplier) {
        //Log.info("runTest | The thread name is: " + Thread.currentThread().getName());
        if (endTime > System.currentTimeMillis()) {
            rateLimiter.acquirePermission(); // This will run on virtual thread
            HttpRequestAndBodyModel httpRequestAndBodyModel = httpRequestSupplier.get();
            // This will run on event loop
            Future<HttpResponse<Buffer>> future = httpRequestAndBodyModel
                    .httpRequest()
                    .sendBuffer(httpRequestAndBodyModel.body());
            // The runTest needs to be called on virtual thread as the future completes on event loop, and we want not to block event loop
            // There is a context switching cost associated but that is something that can be improved upon by refactoring the code
            // Kotlin coroutine might be more powerful here, but we want to try Java 21 virtual thread as part of hackathon
            future.onComplete(h -> Thread.ofVirtual().start(() -> runTest(webClient, httpRequestSupplier)));
        } else {
            HttpRequestAndBodyModel httpRequestAndBodyModel = httpRequestSupplier.get();
            // Sending last message with connection close to cleanup the underlying socket connection
            httpRequestAndBodyModel
                    .httpRequest()
                    .putHeader(HttpHeaders.CONNECTION.toString(), "close")
                    .sendBuffer(httpRequestAndBodyModel.body())
                    .onComplete(h -> {
                        try {
                            webClient.close();
                        } catch (Exception ignored) {
                            // Ignoring the exception as there might be in flight requests.
                        }
                    });

        }
    }
}
