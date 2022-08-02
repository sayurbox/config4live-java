package com.sayurbox.config4live.param;

import org.junit.Assert;
import org.junit.Test;

public class CircuitBreakerParamsTest {

    @Test
    public void buildCircuitBreakerParamsTest() {
        CircuitBreakerParams params = new CircuitBreakerParams(true, 10, 500L, 15L);
        Assert.assertTrue(params.isEnabled());
        Assert.assertEquals(500, params.getSlowCallDurationThreshold().longValue());
        Assert.assertEquals(10, params.getSlidingWindowSize().longValue());
        Assert.assertEquals(15, params.getWaitDurationInOpenState().longValue());
    }

}
