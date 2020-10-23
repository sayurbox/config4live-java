package com.sayurbox.config4live;

import java.util.Objects;

public class Config {

    private String name;
    private String value;
    private FormatType format;

    public Config() {
    }

    public Config(String name, String value, FormatType format) {
        this.name = name;
        this.value = value;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(name, config.name) &&
                Objects.equals(value, config.value) &&
                format == config.format;
    }

}
