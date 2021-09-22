package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.FormatType;
import com.sayurbox.config4live.param.HystrixParams;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpServiceCommandTest {

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
    public void HttpServiceCommand_TimeoutRequest_ShouldFallbackNull() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(successResponse())
                .setResponseCode(200)
                .throttleBody(1, 5, TimeUnit.SECONDS);

        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpServiceCommand cmd = new HttpServiceCommand(new OkHttpClient(),
                baseMockServerUrl(url), "test", provideHystrixParams(1_000));
        Config actual = cmd.execute();
        Assert.assertNull(actual);
    }

    @Test
    public void HttpServiceCommand_errorResponse() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(errorResponse())
                .setResponseCode(400);
        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpServiceCommand cmd = new HttpServiceCommand(new OkHttpClient(),
                baseMockServerUrl(url), "test", provideHystrixParams(1_000));
        Config actual = cmd.execute();
        Assert.assertNull(actual);
    }

    @Test
    public void HttpServiceCommand_configNotFound() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(configNotFoundResponse())
                .setResponseCode(200);
        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpServiceCommand cmd = new HttpServiceCommand(new OkHttpClient(),
                baseMockServerUrl(url), "test", provideHystrixParams(1_000));
        Config actual = cmd.execute();
        Assert.assertNull(actual);
    }

    @Test
    public void HttpServiceCommand_successResponse() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(successResponse())
                .setResponseCode(200);
        server.enqueue(response);
        server.start();
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test_name");
        HttpServiceCommand cmd = new HttpServiceCommand(new OkHttpClient(),
                baseMockServerUrl(url), "test_name", provideHystrixParams(1_000));
        Config actual = cmd.execute();
        Assert.assertNotNull(actual);
        Assert.assertEquals("test_name", actual.getName());
        Assert.assertEquals("test value", actual.getValue());
        Assert.assertEquals(FormatType.text, actual.getFormat());
    }

    private String successResponse() {
        return "{\"error\":null,\"data\":{\"id\":1,\"uuid\":\"uid123\",\"createdBy\":\"\"," +
                "\"createdAt\":1605527603000,\"updatedBy\":\"\",\"updatedAt\":1626071317000,\"isActive\":true," +
                "\"name\":\"test_name\",\"value\":\"test value\",\"description\":\"test only\",\"format\":\"text\"," +
                "\"owner\":\"tester\"},\"success\":true}";
    }

    private String configNotFoundResponse() {
        return "{\"error\":null,\"data\":null,\"success\":true}";
    }

    private String errorResponse() {
        return "{\"error\":\"unknown error\",\"data\":null,\"success\":false}";
    }

    private HystrixParams provideHystrixParams(Integer timeout) {
        return new HystrixParams(timeout, 500, 20, 100, 100);
    }

    private String baseMockServerUrl(HttpUrl url) {
        return String.format("http://localhost:%d", url.url().getPort());
    }

}
