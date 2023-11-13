package com.redhat.hackathon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;

/**
 * The final HTTP request and the body to be sent
 *
 * @param httpRequest The HTTP request
 * @param body        The body to be sent. In case of GET, this is empty buffer
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HttpRequestAndBodyModel(HttpRequest<Buffer> httpRequest, Buffer body) {
}
