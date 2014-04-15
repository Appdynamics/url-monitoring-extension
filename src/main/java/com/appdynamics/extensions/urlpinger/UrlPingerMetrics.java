package com.appdynamics.extensions.urlpinger;

import java.util.Date;

/**
 * Created by kunal.gupta on 4/10/14.
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
