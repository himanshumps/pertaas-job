package com.redhat.hackathon;

import com.couchbase.client.java.Bucket;
import com.redhat.hackathon.metrics.MetricsUtil;
import com.redhat.hackathon.model.RequestModel;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;

@QuarkusMain
public class ApplicationMain implements QuarkusApplication {
  @ConfigProperty(name = "requestJson")
  String requestJson;

  @Inject
  EventBus eventBus;

  @Inject
  Vertx vertx;

  @Inject
  Bucket bucket;

  @ConfigProperty(name = "jobId")
  String jobId;

  @Override
  public int run(String... args) throws Exception {
    Metrics.globalRegistry
        .config()
        .meterFilter(
            new MeterFilter() {
              @Override
              public MeterFilterReply accept(Meter.Id meterId) {
                // Only allow the ones with id tag as those are the ones that we are interested in. If additional metrics is needed, it can be enabled or disabled here
                //Log.info(meterId.getName() + ": " + meterId.getTags());
                if (meterId.getTag("jobId") == null) {
                  return MeterFilterReply.DENY;
                }
                return MeterFilter.super.accept(meterId);
              }

              @Override
              public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {
                //Log.info(id.getName() + ": " + id.getTags());
                if (id.getTag("jobId") != null) {
                  return DistributionStatisticConfig.builder()
                      .percentilesHistogram(true)
                      .percentiles(0., 0.33, 0.50, 0.66, 0.90, 0.95, 0.99, 0.999, 0.9999)
                      .build()
                      .merge(config);
                } else {
                  return DistributionStatisticConfig.builder()
                      .percentilesHistogram(false)
                      .build()
                      .merge(config);
                }
              }
            }
        );
    RequestModel requestModel = Json.decodeValue(requestJson, RequestModel.class);
    requestModel.setEndTime(System.currentTimeMillis() + Duration.ofSeconds(requestModel.getRunDurationInSeconds()).toMillis());
    String consumer = "http_1.1_consumer";
    if (requestModel.getHttpVersion() == null) {
      requestModel.setHttpVersion(HttpVersion.HTTP_1_1);
    }
    if (requestModel.getHttpVersion().equals(HttpVersion.HTTP_2)) {
      consumer = "http_2_consumer";
    }
    for (int i = 0; i < requestModel.getMaxConnections(); i++) {
      eventBus.send(consumer, requestModel, new DeliveryOptions().setLocalOnly(true));
    }
    vertx.setTimer(Duration.ofSeconds(requestModel.getRunDurationInSeconds() + 5).toMillis(), handler -> {
      Log.info("SimpleMeterRegistry response: ");
      Metrics.globalRegistry.getRegistries().forEach(registry -> {
        if (registry instanceof SimpleMeterRegistry) {
          Instant instant = Instant.now();
          String key_tx = jobId + "::" + instant;
          JsonObject jsonObject = MetricsUtil.snapshot(registry, null);

          jsonObject.put("registry", "SimpleMeterRegistry");
          jsonObject.put("key_tx", key_tx);
          // Blocking insert as we do not wish to lose this message before shutting down the application
          bucket.defaultCollection().upsert(key_tx, jsonObject.encode());
          Log.info(jsonObject.encode());
          // TODO: upsert in couchbase as well
          Quarkus.asyncExit();

        }
      });
    });
    Quarkus.waitForExit();
    return 0;
  }
}