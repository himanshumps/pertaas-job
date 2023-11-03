package com.redhat.hackathon.model;

import io.vertx.core.buffer.Buffer;

import java.util.Map;

public record HttpRequestModel(String method, String uri, Map<String, String> headers, Map<String, String> queryParams, Map<String, String> pathParams, Buffer body) {
}
