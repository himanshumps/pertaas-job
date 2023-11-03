package com.redhat.hackathon.model;

import io.vertx.ext.web.client.HttpRequest;

import io.vertx.core.buffer.Buffer;

public record HttpRequestAndBodyModel(HttpRequest<Buffer> httpRequest, Buffer body){

}
