package com.sayurbox.config4live.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandProperties;
import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.param.HystrixParams;

public abstract class ServiceCommand<T> extends HystrixCommand<Config> {

    protected final String configKey;

    public ServiceCommand(String configKey, HystrixParams params) {
        super(hystrixProperties(params));
        this.configKey = configKey;
    }

    private static Setter hystrixProperties(HystrixParams params) {
        HystrixCommand.Setter config = HystrixCommand.Setter.withGroupKey(params.getHystrixCommandGroupKey());
        HystrixCommandProperties.Setter properties = HystrixCommandProperties.Setter();
        properties.withExecutionTimeoutInMilliseconds(params.getExecutionTimeout());
        properties.withCircuitBreakerSleepWindowInMilliseconds(params.getCircuitBreakerSleepWindow());
        properties.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.THREAD);
        properties.withCircuitBreakerEnabled(true);
        properties.withCircuitBreakerRequestVolumeThreshold(params.getCircuitBreakerRequestVolumeThreshold());
        properties.withMetricsRollingStatisticalWindowInMilliseconds(params.getMetricRollingStatisticalWindow());
        config.andCommandPropertiesDefaults(properties);
        return config;
    }

    @Override
    protected Config run() throws Exception {
        T response = requestCommand(configKey);
        if (response == null) {
            return null;
        }
        return parseResponse(response);
    }

    protected abstract T requestCommand(String param);
    protected abstract Config parseResponse(T response);
}
