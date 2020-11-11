package com.sayurbox.config4live;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sayurbox.config4live.source.GrpcConfigurationSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class ConfigurationProviderTest {

    private GrpcConfigurationSource configurationSource;
    private LoadingCache<String, String> caches;
    @Before
    public void before() {
        configurationSource = mock(GrpcConfigurationSource.class);
        caches = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(2, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                            @Override
                            public String load(String key) throws Exception {
                                return key.toUpperCase();
                            }
                });
        caches.cleanUp();
    }

    @Test
    public void bind_Integer_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_config")).thenReturn(null);
        Integer actual = provider.bind("test_config", 5);
        Assert.assertEquals(5, actual.intValue());
    }

    @Test
    public void bind_Integer_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("10");
        when(configurationSource.getProperty("test_config")).thenReturn(config);
        Integer actual = provider.bind("test_config", 5);
        Assert.assertEquals(10, actual.intValue());
    }

    @Test
    public void bind_Integer_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_config", "7");
        Whitebox.setInternalState(provider, "configCache", caches);
        Integer actual = provider.bind("test_config", 5);
        Assert.assertEquals(7, actual.intValue());
        verify(configurationSource, never()).getProperty("test_config");
    }

    @Test
    public void bind_Long_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_config_long")).thenReturn(null);
        Long actual = provider.bind("test_config_long", 6L);
        Assert.assertEquals(6, actual.longValue());
    }

    @Test
    public void bind_Long_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("12");
        when(configurationSource.getProperty("test_config_long")).thenReturn(config);
        Long actual = provider.bind("test_config_long", 13L);
        Assert.assertEquals(12, actual.longValue());
    }

    @Test
    public void bind_Long_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_config_long", "8");
        Whitebox.setInternalState(provider, "configCache", caches);
        Long actual = provider.bind("test_config_long", 10L);
        Assert.assertEquals(8, actual.longValue());
        verify(configurationSource, never()).getProperty("test_config_long");
    }

    @Test
    public void bind_Boolean_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_bool")).thenReturn(null);
        Boolean actual = provider.bind("test_bool", true);
        Assert.assertTrue(actual);
    }

    @Test
    public void bind_Boolean_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("false");
        when(configurationSource.getProperty("test_bool")).thenReturn(config);
        Boolean actual = provider.bind("test_bool", true);
        Assert.assertFalse(actual);
    }

    @Test
    public void bind_Boolean_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_bool", "true");
        Whitebox.setInternalState(provider, "configCache", caches);
        Boolean actual = provider.bind("test_bool", false);
        Assert.assertTrue(actual);
        verify(configurationSource, never()).getProperty("test_bool");
    }

    @Test
    public void bind_Double_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_double")).thenReturn(null);
        Double actual = provider.bind("test_double", 1.5);
        Assert.assertEquals(1.5, actual, 0);
    }

    @Test
    public void bind_Double_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("10.25");
        when(configurationSource.getProperty("test_double")).thenReturn(config);
        Double actual = provider.bind("test_double", 4.57);
        Assert.assertEquals(10.25, actual, 0);
    }

    @Test
    public void bind_Double_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_double", "9.1234");
        Whitebox.setInternalState(provider, "configCache", caches);
        Double actual = provider.bind("test_double", 2.5);
        Assert.assertEquals(9.1234, actual, 0);
        verify(configurationSource, never()).getProperty("test_double");
    }

    @Test
    public void bind_BigDecimal_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_decimal")).thenReturn(null);
        BigDecimal actual = provider.bind("test_decimal", BigDecimal.ZERO);
        Assert.assertEquals(BigDecimal.ZERO, actual);
    }

    @Test
    public void bind_BigDecimal_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("10.25");
        when(configurationSource.getProperty("test_decimal")).thenReturn(config);
        BigDecimal actual = provider.bind("test_decimal", BigDecimal.ZERO);
        Assert.assertEquals(new BigDecimal("10.25"), actual);
    }

    @Test
    public void bind_BigDecimal_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_decimal", "9.1234");
        Whitebox.setInternalState(provider, "configCache", caches);
        BigDecimal actual = provider.bind("test_decimal", BigDecimal.ONE);
        Assert.assertEquals(new BigDecimal("9.1234"), actual);
        verify(configurationSource, never()).getProperty("test_decimal");
    }

    @Test
    public void bind_String_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_string")).thenReturn(null);
        String actual = provider.bind("test_string", "default value");
        Assert.assertEquals("default value", actual);
    }

    @Test
    public void bind_String_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("text query");
        when(configurationSource.getProperty("test_string")).thenReturn(config);
        String actual = provider.bind("test_string", "test default");
        Assert.assertEquals("text query", actual);
    }

    @Test
    public void bind_String_FoundAndCached() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 10);
        Config config = new Config();
        config.setValue("text query");
        config.setName("test_string");
        Whitebox.setInternalState(provider, "configCache", caches);
        when(configurationSource.getProperty("test_string")).thenReturn(config);
        String actual = provider.bind("test_string", "test default");
        Assert.assertEquals("text query", actual);
    }

    @Test
    public void bind_String_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_string", "text query");
        Whitebox.setInternalState(provider, "configCache", caches);
        String actual = provider.bind("test_string", "test default");
        Assert.assertEquals("text query", actual);
        verify(configurationSource, never()).getProperty("test_string");
    }

    @Test
    public void bind_Bean_NotFound_GetDefault() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        when(configurationSource.getProperty("test_bean")).thenReturn(null);
        Person actual = provider.bind("test_bean", Person.class);
        Assert.assertNull(actual);
    }

    @Test
    public void bind_Bean_Found() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, false, 0);
        Config config = new Config();
        config.setValue("{\"id\":2,\"name\":\"john doe\"}");
        when(configurationSource.getProperty("test_bean")).thenReturn(config);
        Person actual = provider.bind("test_bean", Person.class);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, (int) actual.id);
        Assert.assertEquals("john doe", actual.name);
    }

    @Test
    public void bind_Bean_FormCache() {
        ConfigurationProvider provider = new ConfigurationProvider(configurationSource, true, 100);
        caches.put("test_bean", "{\"id\":2,\"name\":\"john doe\"}");
        Whitebox.setInternalState(provider, "configCache", caches);
        Person actual = provider.bind("test_bean", Person.class);
        Assert.assertNotNull(actual);
        Assert.assertEquals(2, (int) actual.id);
        Assert.assertEquals("john doe", actual.name);
        verify(configurationSource, never()).getProperty("test_bean");
    }

    @Test
    public void providerWithBuilder() {
        ConfigurationProvider provider = new ConfigurationProvider.Builder().withSource(configurationSource)
                .withCache(false).withTimeToLive(0).build();
        Config config = new Config();
        config.setValue("text query");
        when(configurationSource.getProperty("test_string")).thenReturn(config);
        String actual = provider.bind("test_string", "test default");
        Assert.assertEquals("text query", actual);
    }

    static class Person {
        public Integer id;
        public String name;
    }
}
