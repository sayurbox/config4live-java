package com.sayurbox.config4live.param;

import com.netflix.hystrix.HystrixCommandGroupKey;

public class HystrixParams {

    private final Integer executionTimeout;
    private final Integer circuitBreakerSleepWindow;
    private final Integer circuitBreakerRequestVolumeThreshold;
    private final Integer metricRollingStatisticalWindow;
    private final Integer metricsHealthSnapshotInterval;

    public HystrixParams(Integer executionTimeout, Integer circuitBreakerSleepWindow,
                         Integer circuitBreakerRequestVolumeThreshold, Integer metricRollingStatisticalWindow,
                         Integer metricsHealthSnapshotInterval) {
        this.executionTimeout = executionTimeout;
        this.circuitBreakerSleepWindow = circuitBreakerSleepWindow;
        this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
        this.metricRollingStatisticalWindow = metricRollingStatisticalWindow;
        this.metricsHealthSnapshotInterval = metricsHealthSnapshotInterval;
    }

    public Integer getExecutionTimeout() {
        return executionTimeout;
    }

    public Integer getCircuitBreakerSleepWindow() {
        return circuitBreakerSleepWindow;
    }

    public Integer getCircuitBreakerRequestVolumeThreshold() {
        return circuitBreakerRequestVolumeThreshold;
    }

    public Integer getMetricRollingStatisticalWindow() {
        return metricRollingStatisticalWindow;
    }

    public Integer getMetricsHealthSnapshotInterval() {
        return metricsHealthSnapshotInterval;
    }

    public HystrixCommandGroupKey getHystrixCommandGroupKey() {
        return HystrixCommandGroupKey.Factory.asKey("live-configuration-client");
    }

}
