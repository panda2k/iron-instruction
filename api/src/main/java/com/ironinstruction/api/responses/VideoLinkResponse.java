package com.ironinstruction.api.responses;

public class VideoLinkResponse {
    private String url;

    public VideoLinkResponse() { }

    public VideoLinkResponse(String url) {
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
