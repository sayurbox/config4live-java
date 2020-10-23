package com.sayurbox.config4live.source;

import com.sayurbox.config4live.Config;

public interface ConfigurationSource {
    Config getProperty(String key);
}
