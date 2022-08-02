package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.command.HttpServiceCommand;
import com.sayurbox.config4live.param.CircuitBreakerParams;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpConfigurationSource implements ConfigurationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConfigurationSource.class);

    private final OkHttpClient httpClient;
    private final String url;
    private final CircuitBreakerParams circuitBreakerParams;
    private final CircuitBreaker circuitBreaker;

    public HttpConfigurationSource(String url,
                                   CircuitBreakerParams circuitBreakerParams,
                                   long executionTimeout, boolean logEnabled) {
        this.url = url;
        this.circuitBreakerParams = circuitBreakerParams;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (logEnabled) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(executionTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(executionTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(executionTimeout, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build();

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
                .circuitBreaker("httpConfigurationKey");

    }

    @Override
    public Config getProperty(String key) {
        LOGGER.debug("get property {} from http source", key);
        System.out.println("get property from http source" + key);
        HttpServiceCommand cmd = new HttpServiceCommand(this.httpClient,
                url,
                key,
                this.circuitBreaker,
                this.circuitBreakerParams.isEnabled());
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

        public Builder withUrl(String url) {
            baseUrl = url;
            return this;
        }

        public Builder withExecutionTimeout(int executionTimeout) {
            this.executionTimeout = executionTimeout;
            return this;
        }

        public Builder withCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
            return this;
        }

        public Builder withCircuitBreakerFailureVolumeThreshold(int failureVolumeThreshold) {
            this.failureVolumeThreshold = failureVolumeThreshold;
            return this;
        }

        public Builder withCircuitBreakerSlowResponseThreshold(int slowCallDurationThreshold) {
            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        public Builder withCircuitBreakerWaitDurationOpenState(int waitDuration) {
            this.waitDuration = waitDuration;
            return this;
        }

        public Builder withLoggerEnabled(boolean loggerEnabled) {
            this.logEnabled = loggerEnabled;
            return this;
        }

        public HttpConfigurationSource build() {
            CircuitBreakerParams circuitBreakerParams = new CircuitBreakerParams(
                    circuitBreakerEnabled,
                    failureVolumeThreshold,
                    slowCallDurationThreshold.longValue(),
                    waitDuration.longValue());
            return new HttpConfigurationSource(baseUrl, circuitBreakerParams, executionTimeout, logEnabled);

        }
    }
}
