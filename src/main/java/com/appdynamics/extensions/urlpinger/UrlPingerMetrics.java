package com.appdynamics.extensions.urlpinger;

/**
 * Metrics for url pinger extension.
 */
public class UrlPingerMetrics {
    private String displayName;
    private int statusCode;
    private long responseTimeInMs;

    public UrlPingerMetrics(String displayName, int statusCode,long responseTimeInMs) {
        this.displayName = displayName;
        this.statusCode = statusCode;
        this.responseTimeInMs = responseTimeInMs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getResponseTimeInMs() {
        return responseTimeInMs;
    }

}
