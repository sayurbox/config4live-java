package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.command.HttpServiceCommand;
import com.sayurbox.config4live.param.HystrixParams;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpConfigurationSource implements ConfigurationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConfigurationSource.class);

    private final HystrixParams hystrixParams;
    private final OkHttpClient httpClient;
    private final String url;

    public HttpConfigurationSource(String url, HystrixParams hystrixParams) {
        this.hystrixParams = hystrixParams;
        this.httpClient = new OkHttpClient();
        this.url = url;
    }

    @Override
    public Config getProperty(String key) {
        LOGGER.debug("get property {} from http source", key);
        HttpServiceCommand cmd = new HttpServiceCommand(this.httpClient, url, key, hystrixParams);
        return cmd.execute();
    }

    public static class Builder {
        private String httpUrl;
        private Integer hystrixExecutionTimeout = 1000;
        private Integer hystrixCircuitBreakerSleepWindow = 1000;
        private Integer hystrixCircuitBreakerRequestVolumeThreshold = 10;
        private Integer hystrixRollingStatisticalWindow = 10000;
        private Integer hystrixHealthSnapshotInterval = 500;

        public Builder() {}

        public Builder withUrl (String url) {
            httpUrl = url;
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

        public HttpConfigurationSource build() {
            HystrixParams hystrixParams =
                    new HystrixParams(hystrixExecutionTimeout, hystrixCircuitBreakerSleepWindow,
                            hystrixCircuitBreakerRequestVolumeThreshold, hystrixRollingStatisticalWindow,
                            hystrixHealthSnapshotInterval);
            return new HttpConfigurationSource(httpUrl, hystrixParams);

        }
    }
}
