package com.redhat.hackathon.consumer;


import com.redhat.hackathon.HttpRequestSupplier;
import com.redhat.hackathon.model.HttpRequestAndBodyModel;
import com.redhat.hackathon.model.RequestModel;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.uritemplate.UriTemplate;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class HTTPConsumerFor_2 {

  @Inject
  Vertx vertx;

  @ConfigProperty(name = "jobId")
  String jobId;

  private static AtomicInteger initCounter = new AtomicInteger(0);
  private static RateLimiter rateLimiter = null;


  private long endTime;

  @ConsumeEvent(value = "http_2_consumer")
  @RunOnVirtualThread
  public void consume(RequestModel requestModel) {
    if (initCounter.incrementAndGet() == 1) {
      rateLimiter = RateLimiter.of(jobId, RateLimiterConfig.custom()
          .limitForPeriod(requestModel.getRequestPerSecond())
          .limitRefreshPeriod(Duration.ofSeconds(1))
          .timeoutDuration(Duration.ofSeconds(5))
          .build());
    }
    // Webclient needs to be created in consume method.
    // We want to run as many connections as specified in maxConnectiosn to ensure that we have that many connection open
    // As this is for HTTP 1.1, we do not want to upgrade to HTTP/2 even though the server supports it
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
    HttpRequestSupplier httpRequestSupplier = new HttpRequestSupplier() {
      final int listSize = requestModel.getHttpRequests().size();
      int counter = 0;

      @Override
      public HttpRequestAndBodyModel get() {
        try {
          int counterModListSize = counter % listSize;
          HttpRequest<Buffer> bufferHttpRequest = webClient.request(
              HttpMethod.valueOf(requestModel.getHttpRequests().get(counterModListSize).method()),
              requestModel.getPort(),
              requestModel.getHostname(),
              UriTemplate.of(requestModel.getHttpRequests().get(counterModListSize).uri()));
          bufferHttpRequest.putHeader("x-path", requestModel.getHttpRequests().get(counterModListSize).uri());
          // Apply the query parameters
          if (requestModel.getHttpRequests().get(counterModListSize).queryParams() != null && !requestModel.getHttpRequests().get(counterModListSize).queryParams().isEmpty()) {
            requestModel.getHttpRequests().get(counterModListSize).queryParams().forEach(bufferHttpRequest::addQueryParam);
          }
          // Apply the path params
          if (requestModel.getHttpRequests().get(counterModListSize).pathParams() != null && !requestModel.getHttpRequests().get(counterModListSize).pathParams().isEmpty()) {
            requestModel.getHttpRequests().get(counterModListSize).pathParams().forEach(bufferHttpRequest::setTemplateParam);
          }
          // Add the headers as specified in the request
          if (requestModel.getHttpRequests().get(counterModListSize).headers() != null && !requestModel.getHttpRequests().get(counterModListSize).headers().isEmpty()) {
            requestModel.getHttpRequests().get(counterModListSize).headers().forEach(bufferHttpRequest::putHeader);
          }
          // SSL
          bufferHttpRequest.ssl(requestModel.isSsl());
          Buffer body = Buffer.buffer();
          // Apply the body if present
          if (requestModel.getHttpRequests().get(counterModListSize).body() != null) {
            body = requestModel.getHttpRequests().get(counterModListSize).body();
          }
          if (counter++ == listSize) {
            counter = 0;
          }
          return new HttpRequestAndBodyModel(bufferHttpRequest, body);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    };
    endTime = requestModel.getEndTime();
    for (int i = 0; i < requestModel.getMaxConcurrentStreams(); i++) {
      runTest(httpRequestSupplier);
    }
  }

  void runTest(HttpRequestSupplier httpRequestSupplier) {
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
      future.onComplete(h -> Thread.ofVirtual().start(() -> runTest(httpRequestSupplier)));
    }
  }
}
