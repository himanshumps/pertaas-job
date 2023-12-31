package com.redhat.hackathon.bean;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.ClusterEnvironment;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.time.Duration;

@ApplicationScoped
public class StartupAndStopEventBean {

    @Inject
    Bucket bucket;
    @Inject
    Cluster cluster;
    @Inject
    ClusterEnvironment clusterEnvironment;

    // Initialize couchbase on startup. Could have done with @Startup but this gives more control.
    void onStart(@Observes StartupEvent ev) {
        Log.info("The couchbase connection is being initiated");
        bucket.getClass();
        Log.info("The couchbase connection has being initiated");
    }

    // Shutdown the couchbase client on shutdown
    void onStop(@Observes ShutdownEvent ev) {
        Log.info("The application is stopping...");
        try {
            cluster.disconnect(Duration.ofMinutes(1));
        } finally {
            clusterEnvironment.shutdown(Duration.ofMinutes(1));
        }
    }
}
