package com.redhat.hackathon.model;

import io.vertx.core.http.HttpVersion;

import java.util.List;

public record RequestModel(String identifier, String key_tx, String protocol, String hostname, int counter, int port, int maxStrems, int headerTableSize, int initialWindowSize, int maxFrameSize, int headerListSize, int maxConnections, int runDurationInSeconds, long startTime, long endTime, HttpVersion httpVersion, int requestPerSecond, List<HttpRequestModel> httpRequests) {
}
