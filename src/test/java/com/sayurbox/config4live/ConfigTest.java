package com.sayurbox.config4live;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void equals() {
        Config first = new Config("test", "test", FormatType.text);
        Config second = new Config("test", "test", FormatType.text);
        Assert.assertEquals(first, second);
        Assert.assertNotEquals(first, new Config("test", "other", FormatType.text));
    }
}
