package com.redhat.hackathon.customizer;

import com.redhat.hackathon.metrics.MetricsUtil;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class VertxOptionsCustomizerApp implements VertxOptionsCustomizer {
    @ConfigProperty(name = "stepDuration", defaultValue = "10")
    int stepDuration;

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
        stepMeterRegistry.start(Executors.defaultThreadFactory());
        vertxOptions
                .setEventLoopPoolSize(CpuCoreSensor.availableProcessors() * 2)
                .setPreferNativeTransport(true)
                .setMetricsOptions(new MicrometerMetricsOptions()
                        .addDisabledMetricsCategory(MetricsDomain.EVENT_BUS)
                        .addDisabledMetricsCategory(MetricsDomain.HTTP_SERVER)
                        .addDisabledMetricsCategory(MetricsDomain.VERTICLES)
                        .setClientRequestTagsProvider(req -> List.of(Tag.of("id", req.headers().get("x-id")), Tag.of("path", req.headers().get("x-path"))))
                        .setLabels(Set.of(Label.HTTP_PATH, Label.HTTP_ROUTE, Label.HTTP_CODE, Label.HTTP_METHOD))
                        .setMicrometerRegistry(compositeMeterRegistry)
                        .setRegistryName("test")
                        .setEnabled(true)
                );
    }

}
