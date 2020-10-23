package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;
import com.sayurbox.config4live.command.GrpServiceCommand;
import com.sayurbox.config4live.param.HystrixParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GrpcConfigurationSource.class })
public class GrpcConfigurationSourceTest {

    private GrpServiceCommand command;

    @Before
    public void before() throws Exception {
        command = mock(GrpServiceCommand.class);
        PowerMockito.whenNew(GrpServiceCommand.class).withAnyArguments().thenReturn(command);
    }

    @Test
    public void getProperty_Found() throws Exception {
        Config config = new Config();
        config.setName("test");
        config.setValue("test");
        doReturn(config).when(command).execute();
        GrpcConfigurationSource src = new GrpcConfigurationSource("config_test", provideHystrixParam());
        Config actual = src.getProperty("key");
        Assert.assertNotNull(actual);
        Assert.assertEquals("test", actual.getValue());
    }

    @Test
    public void getProperty_NotFound() throws Exception {
        doReturn(null).when(command).execute();
        GrpcConfigurationSource src = new GrpcConfigurationSource("config_test", provideHystrixParam());
        Config actual = src.getProperty("key");
        Assert.assertNull(actual);
    }

    private HystrixParams provideHystrixParam() {
        return new HystrixParams(1000, 400, 10, 500);
    }
}
