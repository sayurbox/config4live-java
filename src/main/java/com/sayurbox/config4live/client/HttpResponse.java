package com.sayurbox.config4live.client;

import java.util.Objects;

public class HttpResponse {

    private Boolean success;
    private String error;
    private ConfigurationResponse data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ConfigurationResponse getData() {
        return data;
    }

    public void setData(ConfigurationResponse data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpResponse)) return false;
        HttpResponse that = (HttpResponse) o;
        return Objects.equals(success, that.success) && Objects.equals(error, that.error) &&
                Objects.equals(data, that.data);
    }

}
