package com.redhat.hackathon.model;

import io.vertx.core.http.HttpVersion;

import java.util.List;

public class RequestModel {
    String key_tx;
    String protocol;
    String hostname;
    int counter;
    int port;
    boolean ssl = false;
    int maxStreams;
    int headerTableSize;
    int initialWindowSize;
    int maxFrameSize;
    int headerListSize;
    int maxConnections;
    int runDurationInSeconds;
    long startTime;
    long endTime;
    HttpVersion httpVersion;
    int requestPerSecond;

    public String getKey_tx() {
        return key_tx;
    }

    public void setKey_tx(String key_tx) {
        this.key_tx = key_tx;
    }

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

    public int getMaxStreams() {
        return maxStreams;
    }

    public void setMaxStreams(int maxStreams) {
        this.maxStreams = maxStreams;
    }

    public int getHeaderTableSize() {
        return headerTableSize;
    }

    public void setHeaderTableSize(int headerTableSize) {
        this.headerTableSize = headerTableSize;
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

    public int getHeaderListSize() {
        return headerListSize;
    }

    public void setHeaderListSize(int headerListSize) {
        this.headerListSize = headerListSize;
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

    List<HttpRequestModel> httpRequests;
}
