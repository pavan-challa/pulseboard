package com.pulseboard.dto;

import jakarta.validation.constraints.NotBlank;

public class EndpointRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "url is required")
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
