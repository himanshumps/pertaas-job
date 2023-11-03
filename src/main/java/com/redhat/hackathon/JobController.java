package com.redhat.hackathon;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/job")
public class JobController {

    void onStart(@Observes StartupEvent ev) {
        BackendRegistries.getNow("test")
                .config()
                .meterFilter(
                        new MeterFilter() {
                            @Override
                            public MeterFilterReply accept(Meter.Id meterId) {
                                if (meterId.getTag("id") != null) {
                                    if ("vertx.http.client.active.requests".startsWith(meterId.getName())) {
                                        return MeterFilterReply.DENY;
                                    } else if ("vertx.http.client.request.bytes".startsWith(meterId.getName())) {
                                        return MeterFilterReply.DENY;
                                    } else if ("vertx.http.client.response.bytes".startsWith(meterId.getName())) {
                                        return MeterFilterReply.DENY;
                                    } else if ("vertx.http.client.requests".startsWith(meterId.getName())) {
                                        return MeterFilterReply.DENY;
                                    }
                                }
                                return MeterFilter.super.accept(meterId);
                            }

                            @Override
                            public DistributionStatisticConfig configure(Meter.Id id, DistributionStatisticConfig config) {

                                if ("vertx.http.client.active.requests".startsWith(id.getName())) {
                                    return DistributionStatisticConfig.builder()
                                            .percentilesHistogram(false)
                                            .build()
                                            .merge(config);
                                } else if ("vertx.http.client.request.bytes".startsWith(id.getName())) {
                                    return DistributionStatisticConfig.builder()
                                            .percentilesHistogram(false)
                                            .build()
                                            .merge(config);
                                } else if ("vertx.http.client.response.bytes".startsWith(id.getName())) {
                                    return DistributionStatisticConfig.builder()
                                            .percentilesHistogram(false)
                                            .build()
                                            .merge(config);
                                } else if ("vertx.http.client.requests".startsWith(id.getName())) {
                                    return DistributionStatisticConfig.builder()
                                            .percentilesHistogram(false)
                                            .build()
                                            .merge(config);
                                }
                                return DistributionStatisticConfig.builder()
                                        .percentilesHistogram(true)
                                        .percentiles(0., 0.33, 0.50, 0.66, 0.90, 0.95, 0.99, 0.999, 0.9999)
                                        .build()
                                        .merge(config);
                            }
                        }
                );
    }

    @Inject
    EventBus eventBus;
    @GET
    public CompletionStage<String> startJob() {
        eventBus.send("http_1.1_consumer", JsonObject.of("message", UUID.randomUUID().toString()), new DeliveryOptions().setLocalOnly(true));
        return CompletableFuture.completedFuture(JsonObject.of("success", true).encode());
    }
}
