package com.redhat.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;

import java.util.List;

/**
 * The request JSON that needs to be posted for the application functionality.
 * This is just a generic format and can be changed as per the needs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestModel {
    String protocol;
    String hostname;
    int counter;
    int port;
    boolean ssl = false;
    private long headerTableSize = Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
    private long maxConcurrentStreams = HttpServerOptions.DEFAULT_INITIAL_SETTINGS_MAX_CONCURRENT_STREAMS;
    private int initialWindowSize = Http2Settings.DEFAULT_INITIAL_WINDOW_SIZE;
    private int maxFrameSize = Http2Settings.DEFAULT_MAX_FRAME_SIZE;
    private long maxHeaderListSize = Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE;
    int maxConnections;
    int runDurationInSeconds;
    long startTime;
    long endTime;
    HttpVersion httpVersion;
    int requestPerSecond;
    List<HttpRequestModel> httpRequests;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public long getHeaderTableSize() {
        return headerTableSize;
    }

    public void setHeaderTableSize(long headerTableSize) {
        this.headerTableSize = headerTableSize;
    }

    public long getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    public void setMaxConcurrentStreams(long maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public int getInitialWindowSize() {
        return initialWindowSize;
    }

    public void setInitialWindowSize(int initialWindowSize) {
        this.initialWindowSize = initialWindowSize;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public long getMaxHeaderListSize() {
        return maxHeaderListSize;
    }

    public void setMaxHeaderListSize(long maxHeaderListSize) {
        this.maxHeaderListSize = maxHeaderListSize;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getRunDurationInSeconds() {
        return runDurationInSeconds;
    }

    public void setRunDurationInSeconds(int runDurationInSeconds) {
        this.runDurationInSeconds = runDurationInSeconds;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public int getRequestPerSecond() {
        return requestPerSecond;
    }

    public void setRequestPerSecond(int requestPerSecond) {
        this.requestPerSecond = requestPerSecond;
    }

    public List<HttpRequestModel> getHttpRequests() {
        return httpRequests;
    }

    public void setHttpRequests(List<HttpRequestModel> httpRequests) {
        this.httpRequests = httpRequests;
    }
}
