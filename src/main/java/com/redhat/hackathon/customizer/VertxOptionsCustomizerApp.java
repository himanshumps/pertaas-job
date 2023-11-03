package hackathon.customizer;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.quarkus.vertx.VertxOptionsCustomizer;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
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
                if(compositeMeterRegistry != null) {
                    compositeMeterRegistry.getRegistries().forEach(meterRegistry -> {
                        if(meterRegistry instanceof StepMeterRegistry) {

                        }
                    });
                }
            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return null;
            }
        };
    }

}
