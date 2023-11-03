package com.redhat.hackathon;

import com.redhat.hackathon.model.HttpRequestAndBodyModel;

import java.util.function.Supplier;

@FunctionalInterface
public interface HttpRequestSupplier extends Supplier<HttpRequestAndBodyModel> {
    HttpRequestAndBodyModel get();
}
