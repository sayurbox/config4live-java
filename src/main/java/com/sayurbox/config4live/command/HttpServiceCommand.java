package com.sayurbox.config4live.command;

import com.google.gson.Gson;
import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.FormatType;
import com.sayurbox.config4live.client.ConfigurationResponse;
import com.sayurbox.config4live.client.HttpResponse;
import com.sayurbox.config4live.param.HystrixParams;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpServiceCommand extends ServiceCommand<ConfigurationResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceCommand.class);

    private final OkHttpClient okHttpClient;
    private final String url;
    private final Gson gson;

    public HttpServiceCommand(OkHttpClient okHttpClient, String url,
                              String configKey, HystrixParams params) {
        super(configKey, params);
        this.url = url;
        this.okHttpClient = okHttpClient;
        this.gson = new Gson();
    }

    @Override
    protected ConfigurationResponse requestCommand(String param) {
        String url = String.format("%s/v1/live-configuration/configuration?name=%s",
                this.url, encodeUrlParam(param));

        Request request = new Request.Builder().get().url(url).build();
        Response response = null;
        try {
            LOGGER.debug("requesting live-config: {}", request.url());
            System.out.println("requesting live-config: "+ request.url());
            response = okHttpClient.newCall(request).execute();
            return handleResponse(response);
        } catch (Exception e) {
            LOGGER.error("failed request from live-config, ", e);
            System.out.println("failed request from live-config, "+ e);
            return null;
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }

    @Override
    protected Config parseResponse(ConfigurationResponse response) {
        Config config = new Config();
        config.setName(response.getName());
        config.setValue(response.getValue());
        config.setFormat(FormatType.valueOf(response.getFormat().name()));
        return config;
    }

    protected ConfigurationResponse handleResponse(Response response) throws Exception {
        String body = response.body().string();
        LOGGER.debug("get live-config response: {}", body);
        System.out.println("get live-config response: "+ body);
        if (!response.isSuccessful()) {
            LOGGER.error("Failed response from live-config: {} body {}", response.code(),
                    response.body().string());
            return null;
        }

        HttpResponse httpResponse = gson.fromJson(body, HttpResponse.class);
        if (!httpResponse.getSuccess()) {
            LOGGER.warn("Getting not success response from live-config, error: {}",
                    httpResponse.getError());
            return null;
        }
        return httpResponse.getData();
    }

    @Override
    protected Config getFallback() {
        LOGGER.warn("http command {} fallback is executed", configKey);
        return null;
    }

    private String encodeUrlParam(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("failed encoder url {}, {}", url, e.getMessage());
            return url;
        }
    }

}
