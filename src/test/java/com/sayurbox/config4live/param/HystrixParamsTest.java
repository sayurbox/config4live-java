package com.sayurbox.config4live.param;

import org.junit.Assert;
import org.junit.Test;

public class HystrixParamsTest {

    @Test
    public void hystrixParam() {
        HystrixParams params = new HystrixParams(1000, 400, 20, 100, 500);
        Assert.assertEquals(1000, params.getExecutionTimeout().longValue());
        Assert.assertEquals(400, params.getCircuitBreakerSleepWindow().longValue());
        Assert.assertEquals(20, params.getCircuitBreakerRequestVolumeThreshold().longValue());
        Assert.assertEquals(100, params.getMetricRollingStatisticalWindow().longValue());
        Assert.assertEquals(500, params.getMetricsHealthSnapshotInterval().longValue());
        Assert.assertEquals("live-configuration-client", params.getHystrixCommandGroupKey().name());
    }
}
