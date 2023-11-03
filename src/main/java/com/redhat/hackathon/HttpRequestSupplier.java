package com.redhat.hackathon;

import com.redhat.hackathon.model.HttpRequestAndBodyModel;

import java.util.function.Supplier;

/**
 * To prevent type pollution. More details at https://github.com/RedHatPerf/type-pollution-agent
 */
@FunctionalInterface
public interface HttpRequestSupplier extends Supplier<HttpRequestAndBodyModel> {
    /**
     * Get the HTTP Request and Body to be sent for the request
     * @return The record for HTTP Request and Body
     */
    HttpRequestAndBodyModel get();
}
