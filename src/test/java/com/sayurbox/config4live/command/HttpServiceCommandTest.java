package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.FormatType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        CircuitBreaker circuitBreaker = provideCircuitBreaker(100);
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpServiceCommand cmd = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", circuitBreaker, true);
        Config actual = cmd.execute();
        Assert.assertNull(actual);

    }

    @Test
    public void HttpServiceCommand_CircuitOpen_AfterSlidingWindowThreshold() throws IOException {
        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("Cache-Control", "no-cache")
                .setBody(successResponse())
                .setResponseCode(200)
                .throttleBody(1, 5, TimeUnit.SECONDS);

        server.enqueue(response);
        server.start();
        CircuitBreaker circuitBreaker = provideCircuitBreaker(100);
        Assert.assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
        HttpUrl url = server.url("/v1/live-configuration/configuration?name=test");
        HttpServiceCommand cmd1stCall = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", circuitBreaker, true);
        Config actual = cmd1stCall.execute();
        Assert.assertNull(actual);
        Assert.assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        HttpServiceCommand cmd2ndCall = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", circuitBreaker, true);
        actual = cmd2ndCall.execute();
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
        HttpServiceCommand cmd = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", provideCircuitBreaker(100), true);
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
        HttpServiceCommand cmd = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", provideCircuitBreaker(100), true);
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
        HttpServiceCommand cmd = new HttpServiceCommand(provideOkHttpClient(1, TimeUnit.SECONDS),
                baseMockServerUrl(url), "test", provideCircuitBreaker(100), true);
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

    private String baseMockServerUrl(HttpUrl url) {
        return String.format("http://localhost:%d", url.url().getPort());
    }

    private CircuitBreaker provideCircuitBreaker(long slowCallDuration) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(1)
                .slowCallRateThreshold(70.0f)
                .slowCallDurationThreshold(Duration.ofMillis(slowCallDuration))
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .recordExceptions(IOException.class, TimeoutException.class)
                .build();

        CircuitBreakerRegistry circuitBreakerRegistry =
                CircuitBreakerRegistry.of(circuitBreakerConfig);
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("test");
        return cb;
    }

    private OkHttpClient provideOkHttpClient(long timeout, TimeUnit duration) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, duration)
                .readTimeout(timeout, duration)
                .writeTimeout(timeout, duration)
                .build();
    }

}
