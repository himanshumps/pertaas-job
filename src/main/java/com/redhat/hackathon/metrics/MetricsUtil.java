package com.redhat.hackathon.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.vertx.core.json.JsonObject;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricsUtil {
  public static JsonObject snapshot(MeterRegistry registry, String baseName) {
    Stream<Meter> filtered;
    if (baseName == null) {
      filtered = registry.getMeters().stream();
    } else {
      filtered = registry.getMeters().stream()
          .filter(m -> m.getId().getName().startsWith(baseName));
    }
    LinkedHashMap<String, List<JsonObject>> map = filtered
        .sorted(Comparator.comparing(m -> m.getId().getName()))
        .collect(
            Collectors.groupingBy(m -> m.getId().getName(),
                LinkedHashMap::new,
                Collectors.mapping(MetricsUtil::metricToJson, Collectors.toList())));
    return new JsonObject((Map<String, Object>) (Object) map);
  }
  private static JsonObject metricToJson(Meter meter) {
    JsonObject tags = new JsonObject();
    meter.getId().getTags().forEach(tag -> tags.put(tag.getKey(), tag.getValue()));
    JsonObject obj = new JsonObject().put("tags", tags);
    return meter.match(gauge -> gaugeToJson(obj, gauge),
        counter -> counterToJson(obj, counter),
        timer -> timerToJson(obj, timer),
        summary -> summaryToJson(obj, summary),
        longTaskTimer -> longTaskTimerToJson(obj, longTaskTimer),
        timeGauge -> timeGaugeToJson(obj, timeGauge),
        functionCounter -> functionCounterToJson(obj, functionCounter),
        functionTimer -> functionTimerToJson(obj, functionTimer),
        m -> obj.put("type", "unknown"));
  }
  private static JsonObject summaryToJson(JsonObject obj, DistributionSummary summary) {
    HistogramSnapshot snapshot = summary.takeSnapshot(false);
    return obj.put("type", "summary")
        .put("count", snapshot.count())
        .put("total", snapshot.total())
        .put("mean", snapshot.mean())
        .put("max", snapshot.max());
  }

  private static JsonObject timerToJson(JsonObject obj, Timer timer) {
    return obj.put("type", "timer")
        .put("count", timer.count())
        .put("totalTimeMs", timer.totalTime(TimeUnit.MILLISECONDS))
        .put("meanMs", timer.mean(TimeUnit.MILLISECONDS))
        .put("maxMs", timer.max(TimeUnit.MILLISECONDS));
  }

  private static JsonObject gaugeToJson(JsonObject obj, Gauge gauge) {
    return obj.put("type", "gauge")
        .put("value", gauge.value());
  }

  private static JsonObject counterToJson(JsonObject obj, Counter counter) {
    return obj.put("type", "counter")
        .put("count", counter.count());
  }

  private static JsonObject longTaskTimerToJson(JsonObject obj, LongTaskTimer longTaskTimer) {
    return obj.put("type", "longTaskTimer")
        .put("activeTasks", longTaskTimer.activeTasks())
        .put("durationMs", longTaskTimer.duration(TimeUnit.MILLISECONDS));
  }

  private static JsonObject timeGaugeToJson(JsonObject obj, TimeGauge timeGauge) {
    return obj.put("type", "timeGauge")
        .put("valueMs", timeGauge.value(TimeUnit.MILLISECONDS));
  }

  private static JsonObject functionCounterToJson(JsonObject obj, FunctionCounter functionCounter) {
    return obj.put("type", "functionCounter")
        .put("count", functionCounter.count());
  }

  private static JsonObject functionTimerToJson(JsonObject obj, FunctionTimer functionTimer) {
    return obj.put("type", "functionTimer")
        .put("count", functionTimer.count())
        .put("totalTimeMs", functionTimer.totalTime(TimeUnit.MILLISECONDS))
        .put("meanMs", functionTimer.mean(TimeUnit.MILLISECONDS));
  }
}
