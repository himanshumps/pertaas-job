## PerTaaS Job application
This application is a standalone quarkus 3.5.2 application that takes three arguments as environment variables.
- `JOB_ID`: This is the id that is attached to the micrometer metrics for webclient to make HTTP calls.
- `REQUEST_JSON`: This is the json that specifies various parameters to run the test. The model is available at [HttpRequestModel](src/main/java/com/redhat/hackathon/model/HttpRequestModel.java). There are couple of example given in [examples](src/test/java/com/redhat/hackathon/Examples.java) for various patterns that can be used for the performance test.
- `STEP_DURATION`: This is the scrape interval for persisting the results in couchbase.

There are some common predefined variables that can be used to pass dynamic values

- `${{guid}}`: A uuid-v4 style guid e.g. 611c2e81-2ccb-42d8-9ddc-2d0bfa65c1b4
- `${{timestamp}}`: The current UNIX timestamp in seconds e.g. 1562757107
- `${{isoTimestamp}}`: The current ISO timestamp at zero UTC e.g. 2020-06-09T21:10:36.177Z
- `${{randomUUID}}`: A random 36-character UUID e.g. 6929bb52-3ab2-448a-9796-d6480ecad36b

The application uses the secret/couchbase-secret to connect to couchbase server for persisting the metrics.

The job runs a minimal server to enable metrics scrape by prometheus. The pod should be labeled with `pertaas-job: true` for the podmonitor (podmonitor-for-pertaas-job) to scrape the metrics at an interval of 10s.

The logs are alos printed to console so that they can be streamed into ELK/EFK/Splunk or any console monitoring tool for further analysis and metrics.

This uses minimal async logging as console logging tends to slow down the event-loop or virtual threads.

All the java files in the codebase has been provided with comments for readability and understanding.
