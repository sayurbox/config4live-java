package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.LiveConfigurationGrpc;
import com.sayurbox.config4live.command.GrpServiceCommand;
import com.sayurbox.config4live.param.HystrixParams;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class GrpcConfigurationSource implements ConfigurationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConfigurationSource.class);

    private final HystrixParams hystrixParams;
    private final LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;
    private final ManagedChannel channel;

    public GrpcConfigurationSource(String url, HystrixParams hystrixParams) {
        this.hystrixParams = hystrixParams;
        this.channel = ManagedChannelBuilder.forTarget(requireNonNull(url)).usePlaintext().build();
        this.liveConfigStub = LiveConfigurationGrpc.newBlockingStub(channel);
    }

    @Override
    public Config getProperty(String key) {
        LOGGER.debug("get property {} from grpc source", key);
        GrpServiceCommand cmd = new GrpServiceCommand(liveConfigStub, key, hystrixParams);
        return cmd.execute();
    }

    public static class Builder {

        private String grpcUrl;
        private Integer hystrixExecutionTimeout = 1000;
        private Integer hystrixCircuitBreakerSleepWindow = 1000;
        private Integer hystrixCircuitBreakerRequestVolumeThreshold = 10;
        private Integer hystrixRollingStatisticalWindow = 10000;
        private Integer hystrixHealthSnapshotInterval = 500;

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

        public Builder withHystrixHealthSnapshotInterval(int hystrixHealthSnapshotInterval) {
            this.hystrixHealthSnapshotInterval = hystrixHealthSnapshotInterval;
            return this;
        }

        public GrpcConfigurationSource build() {
            HystrixParams hystrixParams = new HystrixParams(hystrixExecutionTimeout,
                    hystrixCircuitBreakerSleepWindow, hystrixCircuitBreakerRequestVolumeThreshold,
                    hystrixRollingStatisticalWindow, hystrixHealthSnapshotInterval);
            return new GrpcConfigurationSource(grpcUrl, hystrixParams);
        }
    }

}
