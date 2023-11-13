package com.redhat.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    String hostname;
    int port;
    boolean ssl = false;
    private Long headerTableSize = Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
    private Long maxConcurrentStreams = HttpServerOptions.DEFAULT_INITIAL_SETTINGS_MAX_CONCURRENT_STREAMS;
    private Integer initialWindowSize = Http2Settings.DEFAULT_INITIAL_WINDOW_SIZE;
    private Integer maxFrameSize = Http2Settings.DEFAULT_MAX_FRAME_SIZE;
    private Integer maxHeaderListSize = Http2Settings.DEFAULT_MAX_HEADER_LIST_SIZE;
    int maxConnections;
    int runDurationInSeconds;

    long startTime;

    long endTime;
    HttpVersion httpVersion;
    int requestPerSecond;
    List<HttpRequestModel> httpRequests;


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
        return getHttpVersion().equals(HttpVersion.HTTP_2) ?  headerTableSize : null;
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
