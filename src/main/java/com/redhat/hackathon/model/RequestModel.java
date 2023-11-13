package com.redhat.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.http.Http2Settings;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpVersion;

import java.util.List;

/**
 * The request JSON that needs to be posted for the application functionality.
 * This is just a generic format and can be changed as per the needs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestModel {
    // The hostname to run the test for. This can be from a route or can be a cluster local service address
    String hostname;
    // The host port listening for incoming request. Just for reference: The default port for a http url is 80 and for https is 443
    int port;
    // Provide this if the endpoint is secured using certificates
    boolean ssl = false;
    // The number of connections to run the test against. We do not use dynamic pool as want to open as many connections as requested
    int maxConnections;
    // The run duration in seconds for which the test should be ran.
    int runDurationInSeconds;
    // Internal usage
    long startTime;
    // Internal usage
    long endTime;
    // The HTTP version for which the test should be ran. There are three values that are currently supported
    // HTTP_1_0
    // HTTP_1_1
    // HTTP_2
    HttpVersion httpVersion;
    // Rate limiter to allow only the given rps to be executed. Please provide a higher number if you do niot wish to rate limit
    int requestPerSecond;
    // The HTTP request that needs to be executed in round-robin fashion
    List<HttpRequestModel> httpRequests;
    // HTTP/2 header table size
    private Long headerTableSize = Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
    // HTTP/2 max concurrent streams
    private Long maxConcurrentStreams = HttpServerOptions.DEFAULT_INITIAL_SETTINGS_MAX_CONCURRENT_STREAMS;
    // HTTP/2 initial window size
    private Integer initialWindowSize = Http2Settings.DEFAULT_INITIAL_WINDOW_SIZE;
    // HTTP/2 max frame size
    private Integer maxFrameSize = Http2Settings.DEFAULT_MAX_FRAME_SIZE;
    // HTTP/2 max header list size
    private Integer maxHeaderListSize = Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public Long getHeaderTableSize() {
        return getHttpVersion().equals(HttpVersion.HTTP_2) ? headerTableSize : null;
    }

    public void setHeaderTableSize(Long headerTableSize) {
        this.headerTableSize = headerTableSize;
    }

    public Long getMaxConcurrentStreams() {
        return getHttpVersion().equals(HttpVersion.HTTP_2) ? maxConcurrentStreams : null;
    }

    public void setMaxConcurrentStreams(Long maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public Integer getInitialWindowSize() {
        return getHttpVersion().equals(HttpVersion.HTTP_2) ? initialWindowSize : null;
    }

    public void setInitialWindowSize(Integer initialWindowSize) {
        this.initialWindowSize = initialWindowSize;
    }

    public Integer getMaxFrameSize() {
        return getHttpVersion().equals(HttpVersion.HTTP_2) ? maxFrameSize : null;
    }

    public void setMaxFrameSize(Integer maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public Integer getMaxHeaderListSize() {
        return getHttpVersion().equals(HttpVersion.HTTP_2) ? maxHeaderListSize : null;
    }

    public void setMaxHeaderListSize(Integer maxHeaderListSize) {
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
