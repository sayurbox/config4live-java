package com.sayurbox.config4live;

import com.sayurbox.config4live.source.ConfigurationSource;
import com.sayurbox.config4live.source.GrpcConfigurationSource;

public class End2EndTest {

    public static void main(String[] args) {
        ConfigurationSource src = new GrpcConfigurationSource.Builder()
                .withUrl("localhost:5055")
                .withCircuitBreakerEnabled(true)
                .withExecutionTimeout(5000)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerWaitDurationOpenState(15)
                .withCircuitBreakerSlowResponseThreshold(2000)
                .build();
        ConfigurationProvider provider = new ConfigurationProvider.Builder()
                .withSource(src)
                .build();
        String value = provider.bind("order_email_sender_", "none");
        System.out.println("value " + value);
    }

}
