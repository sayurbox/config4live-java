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
}
