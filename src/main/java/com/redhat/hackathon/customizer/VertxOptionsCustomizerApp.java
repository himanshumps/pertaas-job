package com.redhat.hackathon.customizer;

import com.redhat.hackathon.metrics.MetricsUtil;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
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
import io.vertx.micrometer.backends.BackendRegistries;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
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

    @Override
    public void accept(VertxOptions vertxOptions) {
        StepRegistryConfig stepRegistryConfig = new StepRegistryConfig() {
            @Override
            public String prefix() {
                return null;
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
                CompositeMeterRegistry compositeMeterRegistry = (CompositeMeterRegistry) BackendRegistries.getNow("test");
                if (compositeMeterRegistry != null) {
                    compositeMeterRegistry.getRegistries().forEach(meterRegistry -> {
                        if (meterRegistry instanceof StepMeterRegistry) {
                            JsonObject jsonObject = MetricsUtil.snapshot(meterRegistry, null);
                            if (!jsonObject.isEmpty()) {
                                jsonObject.put("registry", "StepMeterRegistry");
                                Log.info(jsonObject.encode());
                                // TODO: upsert in couchbase as well
                            }
                        }
                    });
                }
            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.MILLISECONDS;
            }
        };

        final SimpleMeterRegistry simpleMeterRegistry = new SimpleMeterRegistry() {
            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.MILLISECONDS;
            }
        };
        CompositeMeterRegistry compositeMeterRegistry = new CompositeMeterRegistry();
        compositeMeterRegistry.add(stepMeterRegistry);
        compositeMeterRegistry.add(simpleMeterRegistry);
        compositeMeterRegistry.add(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT));
        compositeMeterRegistry.config().commonTags(List.of(Tag.of("id", jobId)));
        compositeMeterRegistry.getRegistries().forEach(meterRegistry -> meterRegistry.config().commonTags(List.of(Tag.of("jobId", jobId))));
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
                        .setMicrometerRegistry(compositeMeterRegistry)
                        .setRegistryName("test")
                        .setEnabled(true)
                );
    }

}
