package com.sayurbox.config4live.client;

import com.sayurbox.config4live.FormatType;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationResponseTest {

    @Test
    public void equals() {
        ConfigurationResponse p1 = new ConfigurationResponse();
        p1.setName("test");
        p1.setValue("test value");
        p1.setDescription("test description");
        p1.setOwner("tester");
        p1.setFormat(FormatType.text);
        ConfigurationResponse p2 = new ConfigurationResponse();
        p2.setName("test");
        p2.setValue("test value");
        p2.setDescription("test description");
        p2.setOwner("tester");
        p2.setFormat(FormatType.text);
        HttpResponse h1 = new HttpResponse();
        h1.setData(p1);
        h1.setError(null);
        h1.setSuccess(true);
        HttpResponse h2 = new HttpResponse();
        h2.setData(p2);
        h2.setError(null);
        h2.setSuccess(true);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(h1, h2);
        p2.setName("second value");
        Assert.assertNotEquals(p1, p2);
        Assert.assertNotEquals(p1, p2);
    }

}
