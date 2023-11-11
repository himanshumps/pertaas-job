package com.redhat.hackathon.supplier;

import com.redhat.hackathon.model.HttpRequestAndBodyModel;

import java.util.function.Supplier;

/**
 * The supplier to get the next http request to be executed
 * To prevent type pollution. More details at https://github.com/RedHatPerf/type-pollution-agent
 */
@FunctionalInterface
public interface HttpRequestSupplier<T> extends Supplier<HttpRequestAndBodyModel> {
    /**
     * Get the HTTP Request and Body to be sent for the request
     *
     * @return The record for HTTP Request and Body
     */
    HttpRequestAndBodyModel get();


}
