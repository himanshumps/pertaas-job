package com.redhat.hackathon.supplier;

import com.redhat.hackathon.model.HttpRequestAndBodyModel;
import com.redhat.hackathon.model.RequestModel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.uritemplate.UriTemplate;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class HTTPRequestSupplierParameterized {

    /**
     * This is the only method that needs to be modified to support http request creation.
     * It can be used for various purposes like HMAC token, Oauth and various other authN or authZ requirements.
     * The request can be transformed in any way that is required to achieve any business requirement
     *
     * @param requestModel The model that has the details on what needs to be tested
     * @param webClient    Either the HTTP 1.0/ HTTP 1.1 or HTTP/2 client
     * @return The supplier which is used to fetch the request to be executed
     */
    public HttpRequestSupplier<HttpRequestAndBodyModel> getSupplier(RequestModel requestModel, WebClient webClient) {
        return new HttpRequestSupplier<HttpRequestAndBodyModel>() {
            final int listSize = requestModel.getHttpRequests().size();
            int counter = 0;

            @Override
            public HttpRequestAndBodyModel get() {
                try {
                    int counterModListSize = counter % listSize;
                    HttpRequest<Buffer> bufferHttpRequest = webClient.request(
                            HttpMethod.valueOf(requestModel.getHttpRequests().get(counterModListSize).method()),
                            requestModel.getPort(),
                            requestModel.getHostname(),
                            UriTemplate.of(requestModel.getHttpRequests().get(counterModListSize).uri()));
                    bufferHttpRequest.putHeader("x-path", requestModel.getHttpRequests().get(counterModListSize).uri());
                    // Apply the query parameters
                    if (requestModel.getHttpRequests().get(counterModListSize).queryParams() != null && !requestModel.getHttpRequests().get(counterModListSize).queryParams().isEmpty()) {
                        requestModel.getHttpRequests().get(counterModListSize).queryParams().forEach((key, value) -> bufferHttpRequest.setQueryParam(key, replaceCommonVariables(value)));
                    }
                    // Apply the path params
                    if (requestModel.getHttpRequests().get(counterModListSize).pathParams() != null && !requestModel.getHttpRequests().get(counterModListSize).pathParams().isEmpty()) {
                        requestModel.getHttpRequests().get(counterModListSize).pathParams().forEach((key, value) -> bufferHttpRequest.setTemplateParam(key, replaceCommonVariables(value)));
                    }
                    // Add the headers as specified in the request
                    if (requestModel.getHttpRequests().get(counterModListSize).headers() != null && !requestModel.getHttpRequests().get(counterModListSize).headers().isEmpty()) {
                        requestModel.getHttpRequests().get(counterModListSize).headers().forEach((key, value) -> bufferHttpRequest.putHeader(key, replaceCommonVariables(value)));
                    }
                    // SSL
                    bufferHttpRequest.ssl(requestModel.isSsl());
                    Buffer body = Buffer.buffer();
                    // Apply the body if present
                    if (requestModel.getHttpRequests().get(counterModListSize).body() != null) {
                        body = Buffer.buffer(replaceCommonVariables(requestModel.getHttpRequests().get(counterModListSize).body().toString()));
                    }
                    if (counter++ == listSize) {
                        counter = 0;
                    }
                    return new HttpRequestAndBodyModel(bufferHttpRequest, body);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * This method helps in replacing some of the constants to set the value dynamically. It allows
     * ${{guid}},
     * ${{timestamp}},
     * ${{isoTimestamp}}
     * and ${{randomUUID}}
     *
     * @param param The string to replace
     * @return The replaced string
     */
    public String replaceCommonVariables(String param) {
        return param
                .replace("${{guid}}", UUID.randomUUID().toString())
                .replace("${{timestamp}}", String.valueOf(System.currentTimeMillis()))
                .replace("${{isoTimestamp}}", Instant.now().toString())
                .replace("${{randomUUID}}", UUID.randomUUID().toString());

    }
}
