package com.sayurbox.config4live.param;

public class CircuitBreakerParams {

    private final boolean enabled;
    private final Integer slidingWindowSize;
    // in milliseconds
    private final Long slowCallDurationThreshold;
    // in milliseconds
    private final Long waitDurationInOpenState;

    public CircuitBreakerParams(boolean enabled,
                                Integer slidingWindowSize,
                                Long slowCallDurationThreshold,
                                Long waitDurationInOpenState) {
        this.enabled = enabled;
        this.slidingWindowSize = slidingWindowSize;
        this.slowCallDurationThreshold = slowCallDurationThreshold;
        this.waitDurationInOpenState = waitDurationInOpenState;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Integer getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public Long getSlowCallDurationThreshold() {
        return slowCallDurationThreshold;
    }

    public Long getWaitDurationInOpenState() {
        return waitDurationInOpenState;
    }

}
