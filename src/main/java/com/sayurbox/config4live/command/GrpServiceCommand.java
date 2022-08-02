package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.ConfigRequest;
import com.sayurbox.config4live.ConfigResponse;
import com.sayurbox.config4live.FormatType;
import com.sayurbox.config4live.LiveConfigurationGrpc;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class GrpServiceCommand extends ServiceCommandV2<ConfigResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpServiceCommand.class);

    private final LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;
    private final long executionTimeout;

    public GrpServiceCommand(LiveConfigurationGrpc.LiveConfigurationBlockingStub stub,
                             String configKey,
                             CircuitBreaker circuitBreaker,
                             boolean isCircuitBreakerEnabled,
                             long executionTimeout) {
        super(circuitBreaker, isCircuitBreakerEnabled, configKey);
        this.liveConfigStub = stub;
        this.executionTimeout = executionTimeout;
    }

    @Override
    protected ConfigResponse requestCommand(String param) {
        ConfigRequest configRequest = ConfigRequest.newBuilder().setName(param).build();
        try {
            return liveConfigStub.withDeadlineAfter(this.executionTimeout, TimeUnit.MILLISECONDS)
                    .findConfig(configRequest);
        } catch (StatusRuntimeException e) {
            LOGGER.error("RPC failed {} :{}", e.getStatus(), e.getMessage());
            System.out.println("RPC failed {} :{}" + e.getStatus() + e.getMessage());
            return null;
        }
    }

    @Override
    public ConfigResponse getFallback() {
        LOGGER.warn("grpc command {} fallback is executed", configKey);
        return null;
    }

    @Override
    protected Config parseResponse(ConfigResponse response) {
        if (response == null) {
            return null;
        }
        Config config = new Config();
        config.setName(response.getName());
        config.setValue(response.getValue());
        config.setFormat(FormatType.valueOf(response.getFormat().name()));
        return config;
    }

}
