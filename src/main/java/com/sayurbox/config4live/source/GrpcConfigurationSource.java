package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.command.GrpServiceCommand;
import com.sayurbox.config4live.param.HystrixParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcConfigurationSource implements ConfigurationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConfigurationSource.class);

    private final String grpcUrl;
    private final HystrixParams hystrixParams;

    public GrpcConfigurationSource(String url, HystrixParams hystrixParams) {
        this.grpcUrl = url;
        this.hystrixParams = hystrixParams;
    }

    @Override
    public Config getProperty(String key) {
        LOGGER.debug("get property {} from grpc source", key);
        try (GrpServiceCommand cmd = new GrpServiceCommand(grpcUrl, key, hystrixParams)) {
            return cmd.execute();
        }
    }

    public static class Builder {

        private String grpcUrl;
        private Integer hystrixExecutionTimeout = 1000;
        private Integer hystrixCircuitBreakerSleepWindow = 1000;
        private Integer hystrixCircuitBreakerRequestVolumeThreshold = 10;
        private Integer hystrixRollingStatisticalWindow = 1000;

        public Builder() {
        }

        public Builder withGrpcUrl(String url) {
            this.grpcUrl = url;
            return this;
        }

        public Builder withHystrixExecutionTimeout(int hystrixExecutionTimeout) {
            this.hystrixExecutionTimeout = hystrixExecutionTimeout;
            return this;
        }

        public Builder withHystrixCircuitBreakerSleepWindow(int hystrixCircuitBreakerSleepWindow) {
            this.hystrixCircuitBreakerSleepWindow = hystrixCircuitBreakerSleepWindow;
            return this;
        }

        public Builder withHystrixCircuitBreakerRequestVolumeThreshold(
                int hystrixCircuitBreakerRequestVolumeThreshold) {
            this.hystrixCircuitBreakerRequestVolumeThreshold = hystrixCircuitBreakerRequestVolumeThreshold;
            return this;
        }

        public Builder withHystrixRollingStatisticalWindow(int hystrixRollingStatisticalWindow) {
            this.hystrixRollingStatisticalWindow = hystrixRollingStatisticalWindow;
            return this;
        }

        public GrpcConfigurationSource build() {
            HystrixParams hystrixParams = new HystrixParams(hystrixExecutionTimeout,
                    hystrixCircuitBreakerSleepWindow, hystrixCircuitBreakerRequestVolumeThreshold,
                    hystrixRollingStatisticalWindow);
            return new GrpcConfigurationSource(grpcUrl, hystrixParams);
        }
    }

}
