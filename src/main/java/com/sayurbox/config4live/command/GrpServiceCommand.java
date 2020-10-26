package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.ConfigRequest;
import com.sayurbox.config4live.ConfigResponse;
import com.sayurbox.config4live.FormatType;
import com.sayurbox.config4live.LiveConfigurationGrpc;
import com.sayurbox.config4live.param.HystrixParams;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpServiceCommand extends ServiceCommand<ConfigResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpServiceCommand.class);

    private final LiveConfigurationGrpc.LiveConfigurationBlockingStub liveConfigStub;

    public GrpServiceCommand(LiveConfigurationGrpc.LiveConfigurationBlockingStub stub,
                             String configKey, HystrixParams params) {
        super(configKey, params);
        this.liveConfigStub = stub;
    }

    @Override
    protected ConfigResponse requestCommand(String param) {
        ConfigRequest configRequest = ConfigRequest.newBuilder().setName(param).build();
        try {
            return liveConfigStub.findConfig(configRequest);
        } catch (StatusRuntimeException e) {
            LOGGER.error("RPC failed {} :{}", e.getStatus(), e.getMessage());
            return null;
        }
    }

    @Override
    protected Config getFallback() {
        LOGGER.warn("grpc command {} fallback is executed", configKey);
        return null;
    }

    @Override
    protected Config parseResponse(ConfigResponse response) {
        Config config = new Config();
        config.setName(response.getName());
        config.setValue(response.getValue());
        config.setFormat(FormatType.valueOf(response.getFormat().name()));
        return config;
    }

}
