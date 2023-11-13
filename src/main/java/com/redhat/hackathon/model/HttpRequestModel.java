package com.redhat.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.buffer.Buffer;

import java.util.Map;

/**
 * The HTTP request parameters
 *
 * @param method      The HTTP method
 * @param uri         The URI template of the request. Read more about it at https://vertx.io/docs/vertx-uri-template/java/
 * @param headers     The HTTP headers to be added to the request
 * @param queryParams The query params to be added to the request
 * @param pathParams  The path param to be applied to the URI template
 * @param body        The HTTP body to be sent
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HttpRequestModel(String method, String uri, Map<String, String> headers, Map<String, String> queryParams,
                               Map<String, String> pathParams, Buffer body) {
}
