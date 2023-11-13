package com.redhat.hackathon;

import com.redhat.hackathon.model.HttpRequestModel;
import com.redhat.hackathon.model.RequestModel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public class Examples {
    public static void main(String[] args) {

        // Call a single endpoint using HTTP 1.1 with 20 connection using ssl and run for 3 minutes
        RequestModel requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_1_1); // Use protocol 1.1
        requestModel.setSsl(true); // Use SSL
        requestModel.setMaxConnections(20); // Always keep 20 connection open
        requestModel.setRequestPerSecond(Integer.MAX_VALUE); // No rate limit
        requestModel.setHostname("<provide hostname here>");
        requestModel.setPort(443); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz", null, null, null, null)));
        System.out.println("Call a single endpoint using HTTP 1.1 with 20 connection using ssl and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));

        // Call two endpoint (GET and POST) using HTTP 1.1 with 25 connection using ssl and run for 3 minutes
        requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_1_1); // Use protocol 1.1
        requestModel.setSsl(true); // Use SSL
        requestModel.setMaxConnections(25); // Always keep 25 connection open
        requestModel.setRequestPerSecond(Integer.MAX_VALUE); // No rate limit
        requestModel.setHostname("<provide hostname here>");
        requestModel.setPort(443); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz", null, null, null, null),
                new HttpRequestModel(HttpMethod.POST.name(), "/healthz", null, null, null, Buffer.buffer("This is the sample ${{guid}}"))));
        System.out.println("Call two endpoint (GET and POST) using HTTP 1.1 with 25 connection using ssl and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));

        // Call two GET endpoint using path param. The metrics will be collated based on path provided and run for 3 minutes
        requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_1_1); // Use protocol 1.1
        requestModel.setSsl(true); // Use SSL
        requestModel.setMaxConnections(20); // Always keep 20 connection open
        requestModel.setRequestPerSecond(Integer.MAX_VALUE); // No rate limit
        requestModel.setHostname("<provide hostname here>");
        requestModel.setPort(443); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz/{healthEndpoint}", null, null, Map.of("healthEndpoint", "1"), null),
                new HttpRequestModel(HttpMethod.GET.name(), "/healthz/{healthEndpoint}", null, null, Map.of("healthEndpoint", "2"), null)));
        System.out.println("Call two GET endpoint using path param. The metrics will be collated based on path provided and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));

        // Limit 1000 rps and run for 3 minutes
        requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_1_1); // Use protocol 1.1
        requestModel.setSsl(true); // Use SSL
        requestModel.setMaxConnections(30); // Always keep 30 connection open
        requestModel.setRequestPerSecond(1000); // 1000 rps rate limit
        requestModel.setHostname("<provide hostname here>");
        requestModel.setPort(443); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz/{healthEndpoint}", null, null, Map.of("healthEndpoint", "1"), null),
                new HttpRequestModel(HttpMethod.GET.name(), "/healthz/{healthEndpoint}", null, null, Map.of("healthEndpoint", "2"), null)));
        System.out.println("Limit 1000 rps and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));
        // Call using the service url with non ssl, 20 connection and run for 3 minutes
        requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_1_1); // Use protocol 1.1
        requestModel.setSsl(false); // Use SSL
        requestModel.setMaxConnections(20); // Always keep 20 connection open
        requestModel.setRequestPerSecond(Integer.MAX_VALUE); // No rate limit
        requestModel.setHostname("<provide service name here>");
        requestModel.setPort(8080); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz", null, null, null, null)));
        System.out.println("Call using the service url with non ssl, 20 connection and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));


        // Call using HTTP/2 and 20 max streams and run for 3 minutes
        requestModel = new RequestModel();
        requestModel.setHttpVersion(HttpVersion.HTTP_2); // Use protocol HTTP/2
        requestModel.setSsl(true); // Use SSL
        requestModel.setMaxConnections(20); // Always keep 20 connection open
        requestModel.setMaxConcurrentStreams(20L); // 20 streams
        requestModel.setRequestPerSecond(Integer.MAX_VALUE); // No rate limit
        requestModel.setHostname("<provide hostname here>");
        requestModel.setPort(443); // ssl port
        requestModel.setRunDurationInSeconds(3 * 60); // 3 minutes in seconds
        requestModel.setHttpRequests(List.of(new HttpRequestModel(HttpMethod.GET.name(), "/healthz", null, null, null, null)));
        System.out.println("Call using HTTP/2 and 20 max streams and run for 3 minutes: " + removeStartTimeAndEndTime(requestModel));
    }

    public static JsonObject removeStartTimeAndEndTime(RequestModel requestModel) {
        JsonObject jsonObject = JsonObject.mapFrom(requestModel);
        jsonObject.remove("startTime");
        jsonObject.remove("endTime");
        return jsonObject;
    }
}
