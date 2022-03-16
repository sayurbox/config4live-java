package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class HttpConfigurationSourceTest {

    private MockWebServer server;

    @Before
    public void before() {
        server = new MockWebServer();
    }

    @After
    public void after() throws IOException {
        server.shutdown();
    }

    @Test
    public void getProperty_NotFound() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(configNotFoundResponse())
                .setResponseCode(200);
        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpConfigurationSource src = new HttpConfigurationSource.Builder()
                .withUrl(baseMockServerUrl(url))
                .withLoggerEnabled(false)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(5000)
                .build();
        Config actual = src.getProperty("test");
        Assert.assertNull(actual);
    }

    @Test
    public void getProperty_Found() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(configFoundResponse())
                .setResponseCode(200);
        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpConfigurationSource src = new HttpConfigurationSource.Builder()
                .withUrl(baseMockServerUrl(url))
                .withLoggerEnabled(false)
                .withExecutionTimeout(1000)
                .withCircuitBreakerEnabled(true)
                .withCircuitBreakerFailureVolumeThreshold(10)
                .withCircuitBreakerSlowResponseThreshold(1000)
                .withCircuitBreakerWaitDurationOpenState(5000)
                .build();
        Config actual = src.getProperty("test");
        Assert.assertNotNull(actual);
        Assert.assertEquals("test_name", actual.getName());
        Assert.assertEquals("test value", actual.getValue());
    }

    private String configNotFoundResponse() {
        return "{\"error\":null,\"data\":null,\"success\":true}";
    }

    private String configFoundResponse() {
        return "{\"error\":null,\"data\":{\"id\":1,\"uuid\":\"uid123\",\"createdBy\":\"\"," +
                "\"createdAt\":1605527603000,\"updatedBy\":\"\",\"updatedAt\":1626071317000,\"isActive\":true," +
                "\"name\":\"test_name\",\"value\":\"test value\",\"description\":\"test only\",\"format\":\"text\"," +
                "\"owner\":\"tester\"},\"success\":true}";
    }

    private String baseMockServerUrl(HttpUrl url) {
        return String.format("http://localhost:%d", url.url().getPort());
    }

}
