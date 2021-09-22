package com.sayurbox.config4live.client;

import com.sayurbox.config4live.FormatType;

import java.util.Objects;

public class ConfigurationResponse {

    private String name;
    private String value;
    private String description;
    private FormatType format;
    private String owner;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FormatType getFormat() {
        return format;
    }

    public void setFormat(FormatType format) {
        this.format = format;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationResponse that = (ConfigurationResponse) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value)
                && Objects.equals(description, that.description)
                && format == that.format
                && Objects.equals(owner, that.owner);
    }

}
