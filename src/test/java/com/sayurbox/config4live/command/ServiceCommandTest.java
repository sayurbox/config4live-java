package com.sayurbox.config4live.command;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.param.HystrixParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceCommandTest {

    private Supplier<String> runMethod;

    private class TestCommand extends ServiceCommand<String> {

        public TestCommand(HystrixParams params) {
            super("test_config", params);
        }

        @Override
        protected String requestCommand(String param) {
            return runMethod.get();
        }

        @Override
        protected Config parseResponse(String response) {
            Config config = new Config();
            config.setName("test_name");
            config.setValue(response);
            return config;
        }

        @Override
        protected Config getFallback() {
            Config config = new Config();
            config.setName("test_name");
            config.setValue("test_fallback_value");
            return config;
        }
    }

    @Before
    @SuppressWarnings("unchecked")
    public void before() {
        this.runMethod = mock(Supplier.class);
    }

    @Test
    public void serviceCommand_SuccessResponse() {
        HystrixParams params = provideHystrixParam(1000);
        when(runMethod.get()).thenReturn("success_value");
        TestCommand cmd = new TestCommand(params);
        Config actual = cmd.execute();
        Assert.assertNotNull(actual);
        Assert.assertEquals("success_value", actual.getValue());
    }

    @Test
    public void serviceCommand_HasExceptionShouldGetFallback() {
        HystrixParams params = provideHystrixParam(2000);
        when(runMethod.get()).thenThrow(new RuntimeException("unknown error"));
        TestCommand cmd = new TestCommand(params);
        Config actual = cmd.execute();
        Assert.assertNotNull(actual);
        Assert.assertEquals("test_fallback_value", actual.getValue());
    }

    @Test
    public void serviceCommand_TimeoutShouldGetFallback() {
        HystrixParams params = provideHystrixParam(100);
        runMethod = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return "test_timeout";
            }
            return "test_value";
        };
        TestCommand cmd = new TestCommand(params);
        Config actual = cmd.execute();
        Assert.assertNotNull(actual);
        Assert.assertEquals("test_fallback_value", actual.getValue());
    }

    private HystrixParams provideHystrixParam(int timeout) {
        return new HystrixParams(timeout, 400, 10, 500, 500);
    }

}
