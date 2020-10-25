package com.sayurbox.config4live;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.sayurbox.config4live.source.ConfigurationSource;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ConfigurationProvider {

    private final ConfigurationSource source;
    private final Boolean cacheable;
    private final Gson gson;
    private final LoadingCache<String, String> configCache;

    public ConfigurationProvider(ConfigurationSource source, Boolean cacheable, Integer cacheTtl) {
        this.gson = new Gson();
        this.cacheable = cacheable;
        this.source = requireNonNull(source);
        this.configCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cacheTtl, TimeUnit.SECONDS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return key.toUpperCase();
                    }
                });
    }

    public Integer bind(String key, Integer defaultValue) {
        return getValueOrDefault(requestProperty(key), defaultValue, Integer::parseInt);
    }

    public Boolean bind(String key, Boolean defaultValue) {
        return getValueOrDefault(requestProperty(key), defaultValue, Boolean::valueOf);
    }

    public Double bind(String key, Double defaultValue) {
        return getValueOrDefault(requestProperty(key), defaultValue, Double::valueOf);
    }

    public BigDecimal bind(String key, BigDecimal defaultValue) {
        return getValueOrDefault(requestProperty(key), defaultValue, BigDecimal::new);
    }

    public String bind(String key, String defaultValue) {
        return getValueOrDefault(requestProperty(key), defaultValue, s -> s);
    }

    public <T> T bind(String key, Class<T> clazz) {
        return getValueOrDefault(requestProperty(key), null, s -> gson.fromJson(s, clazz));
    }

    private <T> T getValueOrDefault(String value, T defaultValue, Function<String, T> func) {
        if (value == null) {
            return defaultValue;
        }
        return func.apply(value);
    }

    public String requestProperty(String key) {
        if (cacheable) {
            String cacheValue = configCache.getIfPresent(key);
            if (cacheValue != null) {
                return cacheValue;
            }
        }
        Config config = source.getProperty(key);
        if (config == null) {
            return null;
        }
        if (cacheable) {
            configCache.put(key, config.getValue());
        }
        return config.getValue();
    }

    public static class Builder {

        private ConfigurationSource source;
        private Boolean useCache = true;
        private Integer cacheTtl = 0;

        public Builder() {
        }

        public Builder withSource(ConfigurationSource source) {
            this.source = source;
            return this;
        }

        public Builder withCache(boolean useCache) {
            this.useCache = useCache;
            return this;
        }

        public Builder withTimeToLive(int ttl) {
            this.cacheTtl = ttl;
            return this;
        }

        public ConfigurationProvider build() {
            return new ConfigurationProvider(source, useCache, cacheTtl);
        }
    }
}
