package com.sayurbox.config4live;

import com.sayurbox.config4live.source.ConfigurationSource;
import com.sayurbox.config4live.source.GrpcConfigurationSource;
import com.sayurbox.config4live.source.HttpConfigurationSource;

public class LiveConfigurationTest {

    public static void main(String[] args) {

        ConfigurationSource src = new HttpConfigurationSource.Builder()
                .withUrl("https://cons-live-config.sayurbox.co.id")
                .withLoggerEnabled(true)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(5000)
                .build();
        ConfigurationProvider provider = new ConfigurationProvider.Builder().withSource(src)
                .withCache(false).withTimeToLive(0).build();

        ConfigurationSource source = new GrpcConfigurationSource.Builder()
                .withUrl("localhost:5055")
                .withLoggerEnabled(true)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(5000)
                .build();
        ConfigurationProvider provider2 = new ConfigurationProvider.Builder().withSource(source)
                .withCache(true)
                .withTimeToLive(10).build();

        String emailSender = provider2.bind("order_email_sender_3", "test2-email@test.com");
        System.out.println("email sender "+ emailSender);



    }

}
