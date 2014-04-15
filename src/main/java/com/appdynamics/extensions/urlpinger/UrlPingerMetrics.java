package com.appdynamics.extensions.urlpinger;

import java.util.Date;

/**
 * Metrics for url pinger extension.
 */
public class UrlPingerMetrics {
    private String displayName;
    private int statusCode;
    private Date lastSeen;

    public UrlPingerMetrics(String displayName, int statusCode, Date lastSeen) {
        this.displayName = displayName;
        this.statusCode = statusCode;
        this.lastSeen = lastSeen;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
