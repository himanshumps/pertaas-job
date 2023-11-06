package com.redhat.hackathon.customizer;

import com.couchbase.client.java.Bucket;
import com.redhat.hackathon.metrics.MetricsUtil;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.CountingMode;
import io.micrometer.core.instrument.simple.SimpleConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.quarkus.logging.Log;
import io.quarkus.vertx.VertxOptionsCustomizer;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MetricsDomain;
import io.vertx.micrometer.MicrometerMetricsOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Add the custom micrometer backend registry during application startup via Recorder
 */
@ApplicationScoped
public class VertxOptionsCustomizerApp implements VertxOptionsCustomizer {
  @ConfigProperty(name = "stepDuration", defaultValue = "10")
  int stepDuration;

  @ConfigProperty(name = "jobId")
  String jobId;

  @Inject
  Bucket bucket;

  @Override
  public void accept(VertxOptions vertxOptions) {
    StepRegistryConfig stepRegistryConfig = new StepRegistryConfig() {
      @Override
      public String prefix() {
        return "pertaas";
      }

      @Override
      public Duration step() {
        return Duration.ofSeconds(stepDuration);
      }

      @Override
      public String get(String s) {
        return null;
      }
    };
    final StepMeterRegistry stepMeterRegistry = new StepMeterRegistry(stepRegistryConfig, Clock.SYSTEM) {
      @Override
      protected void publish() {
        Instant instant = Instant.now();
        String key_tx = jobId + "::" + instant;
        JsonObject jsonObject = MetricsUtil.snapshot(this, null);
        jsonObject.put("registry", "StepMeterRegistry");
        jsonObject.put("key_tx", key_tx);
        Log.info(jsonObject.encode());
        bucket.reactive().defaultCollection().upsert(key_tx, com.couchbase.client.java.json.JsonObject.fromJson(jsonObject.encode())).subscribe();
      }

      @Override
      protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
      }
    };
    SimpleConfig simpleConfig = new SimpleConfig() {
      @Override
      public String get(String key) {
        return "pertaas";
      }

      @Override
      public Duration step() {
        return Duration.ofHours(24);
      }

      @Override
      public CountingMode mode() {
        return CountingMode.CUMULATIVE;
      }
    };
    final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry(simpleConfig, Clock.SYSTEM) {
      @Override
      protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
      }

    };
    Metrics.globalRegistry.add(stepMeterRegistry);
    Metrics.globalRegistry.add(simpleMeterRegistry);
    CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
    compositeMeterRegistry.add(stepMeterRegistry);
    compositeMeterRegistry.add(simpleMeterRegistry);
    compositeMeterRegistry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
    Metrics.globalRegistry.getRegistries().forEach(meterRegistry -> meterRegistry.config().commonTags(List.of(Tag.of("jobId", jobId))));
    Metrics.globalRegistry.config().commonTags(List.of(Tag.of("jobId", jobId)));
    stepMeterRegistry.start(Executors.defaultThreadFactory());
    // The whole idea of the pertaas tool is to get the metrics
    // Using programmatic access to metrics rather than injection
    vertxOptions
        .setEventLoopPoolSize(CpuCoreSensor.availableProcessors() * 2)
        .setPreferNativeTransport(true)
        .setMetricsOptions(new MicrometerMetricsOptions()
            .addDisabledMetricsCategory(MetricsDomain.EVENT_BUS)
            .addDisabledMetricsCategory(MetricsDomain.HTTP_SERVER)
            .addDisabledMetricsCategory(MetricsDomain.VERTICLES)
            .addDisabledMetricsCategory(MetricsDomain.DATAGRAM_SOCKET)
            .addDisabledMetricsCategory(MetricsDomain.NAMED_POOLS)
            .addDisabledMetricsCategory(MetricsDomain.NET_SERVER)
            .setClientRequestTagsProvider(req -> List.of(Tag.of("path", req.headers().get("x-path"))))
            .setLabels(Set.of(Label.HTTP_PATH, Label.HTTP_ROUTE, Label.HTTP_CODE, Label.HTTP_METHOD))
            .setEnabled(true)
        );
  }

}
