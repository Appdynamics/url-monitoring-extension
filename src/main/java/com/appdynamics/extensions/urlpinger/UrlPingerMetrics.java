package com.appdynamics.extensions.urlpinger;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Metrics for url pinger extension.
 */
public class UrlPingerMetrics {
    private String displayName;
    private int statusCode;
    private long responseTimeInMs;
    private long responseSizeInBytes;

    public UrlPingerMetrics(String displayName, int statusCode,long responseTimeInMs,long responseSizeInBytes) {
        this.displayName = displayName;
        this.statusCode = statusCode;
        this.responseTimeInMs = responseTimeInMs;
        this.responseSizeInBytes = responseSizeInBytes;
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

    public long getResponseSizeInBytes() {
        return responseSizeInBytes;
    }
}
