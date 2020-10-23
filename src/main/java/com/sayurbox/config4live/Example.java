package com.sayurbox.config4live;

import com.sayurbox.config4live.source.ConfigurationSource;
import com.sayurbox.config4live.source.GrpcConfigurationSource;

public class Example {

    public static void main(String[] args) {
        ConfigurationSource source = new GrpcConfigurationSource.Builder()
                .withGrpcUrl("localhost:5055")
                .build();
        ConfigurationProvider provider = new ConfigurationProvider.Builder().withSource(source)
                .withCache(true)
                .withTimeToLive(5000)
                .build();

        String name = provider.bind("default_warehouse", "JJ");
        System.out.println("name "+name);
    }
}
