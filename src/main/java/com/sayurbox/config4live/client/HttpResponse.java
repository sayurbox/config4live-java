package com.sayurbox.config4live.client;

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
}
