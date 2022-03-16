package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.function.Supplier;

public abstract class ServiceCommandV2<T> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceCommandV2.class);

    private final CircuitBreaker circuitBreaker;
    private final boolean isCircuitBreakerEnabled;
    protected final String configKey;

    protected ServiceCommandV2(CircuitBreaker circuitBreaker,
                          boolean isCircuitBreakerEnabled,
                          String configKey) {
        this.circuitBreaker = circuitBreaker;
        this.isCircuitBreakerEnabled = isCircuitBreakerEnabled;
        this.configKey = configKey;
    }

    public Config execute() {
        // ref code examples:
        // https://github.com/thombergs/code-examples/blob/master/resilience4j/timelimiter/src/main/java/io/reflectoring/resilience4j/timelimiter/Examples.java
        Supplier<T> supplier = () -> requestCommand(this.configKey);
        if (!this.isCircuitBreakerEnabled) {
            return parseResponse(supplier.get());
        }
        Decorators.DecorateSupplier<T> decorated = Decorators
                .ofSupplier(supplier)
                .withCircuitBreaker(this.circuitBreaker)
                .withFallback(Collections.singletonList(CallNotPermittedException.class), e -> {
                    logger.warn("execute fallback CallNotPermittedException");
                    return getFallback();
                });
        Supplier<T> decorator = decorated.decorate();
        return parseResponse(decorator.get());
    }

    public abstract T getFallback();

    protected abstract T requestCommand(String param);

    protected abstract Config parseResponse(T response);

}
