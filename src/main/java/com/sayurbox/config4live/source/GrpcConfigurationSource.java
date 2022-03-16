package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.command.GrpServiceCommand;
import com.sayurbox.config4live.param.CircuitBreakerParams;
import com.sayurbox.shared.proto.consliveconfig.LiveConfigurationGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;

public class GrpcConfigurationSource implements ConfigurationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcConfigurationSource.class);

    private final LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;
    private final ManagedChannel channel;
    private CircuitBreakerParams circuitBreakerParams;
    private final long executionTimeout;
    private final CircuitBreaker circuitBreaker;

    public GrpcConfigurationSource(String url, CircuitBreakerParams circuitBreakerParams,
                                   long executionTimeout) {
        this.channel = ManagedChannelBuilder.forTarget(requireNonNull(url)).usePlaintext().build();
        this.liveConfigStub = LiveConfigurationGrpc.newBlockingStub(channel);
        this.circuitBreakerParams = circuitBreakerParams;
        this.executionTimeout = executionTimeout;
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(circuitBreakerParams.getSlidingWindowSize())
                .slowCallRateThreshold(70.0f)
                .slowCallDurationThreshold(Duration.ofMillis(
                        circuitBreakerParams.getSlowCallDurationThreshold()))
                .waitDurationInOpenState(Duration.ofMillis(
                        circuitBreakerParams.getWaitDurationInOpenState()))
                .recordExceptions(IOException.class, TimeoutException.class)
                .build();

        CircuitBreakerRegistry circuitBreakerRegistry =
                CircuitBreakerRegistry.of(circuitBreakerConfig);

        this.circuitBreaker = circuitBreakerRegistry
                .circuitBreaker("grpcConfigurationKey");

    }

    @Override
    public Config getProperty(String key) {
        LOGGER.debug("get property {} from grpc source", key);
        GrpServiceCommand cmd = new GrpServiceCommand(liveConfigStub,
                key,
                this.circuitBreaker,
                this.circuitBreakerParams.isEnabled(),
                this.executionTimeout);
        return cmd.execute();
    }

    public static class Builder {

        private String baseUrl;
        private boolean logEnabled = false;
        private Integer executionTimeout = 5000;
        private boolean circuitBreakerEnabled = true;
        private Integer failureVolumeThreshold = 10;
        private Integer slowCallDurationThreshold = 1000;
        private Integer waitDuration = 20000;

        public Builder() {}

        public GrpcConfigurationSource.Builder withUrl(String url) {
            baseUrl = url;
            return this;
        }

        public GrpcConfigurationSource.Builder withExecutionTimeout(int executionTimeout) {
            this.executionTimeout = executionTimeout;
            return this;
        }

        public GrpcConfigurationSource.Builder withCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
            return this;
        }

        public GrpcConfigurationSource.Builder withCircuitBreakerFailureVolumeThreshold(int failureVolumeThreshold) {
            this.failureVolumeThreshold = failureVolumeThreshold;
            return this;
        }

        public GrpcConfigurationSource.Builder withCircuitBreakerSlowResponseThreshold(int slowCallDurationThreshold) {
            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        public GrpcConfigurationSource.Builder withCircuitBreakerWaitDurationOpenState(int waitDuration) {
            this.waitDuration = waitDuration;
            return this;
        }

        public GrpcConfigurationSource.Builder withLoggerEnabled(boolean loggerEnabled) {
            this.logEnabled = loggerEnabled;
            return this;
        }

        public GrpcConfigurationSource build() {
            CircuitBreakerParams circuitBreakerParams = new CircuitBreakerParams(
                    circuitBreakerEnabled,
                    failureVolumeThreshold,
                    slowCallDurationThreshold.longValue(),
                    waitDuration.longValue());
            return new GrpcConfigurationSource(baseUrl, circuitBreakerParams, executionTimeout);
        }
    }

}
