package com.redhat.hackathon;

import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.quarkus.logging.Log;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.micrometer.backends.BackendRegistries;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/metrics")
public class PrometheusControiller {
    @GET
    public void prometheus(RoutingContext routingContext) {
        Log.info("Inside prometheus");
        CompositeMeterRegistry compositeMeterRegistry = (CompositeMeterRegistry) BackendRegistries.getNow("test");
        compositeMeterRegistry.getRegistries().forEach(registry -> {
            if (registry instanceof PrometheusMeterRegistry){
                PrometheusMeterRegistry promRegistry = (PrometheusMeterRegistry) registry;
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004).end(promRegistry.scrape());
            }
        });
    }
}
