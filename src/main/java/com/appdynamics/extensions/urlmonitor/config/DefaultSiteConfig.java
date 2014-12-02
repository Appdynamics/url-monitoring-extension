package com.appdynamics.extensions.urlmonitor.config;

public class DefaultSiteConfig extends SiteConfigBase {

    public DefaultSiteConfig()
    {
        method = "HEAD";
        socketTimeout = 30000;
        connectTimeout = 30000;
        redirectsAllowed = true;
        maxRedirects = 10;
        numAttempts = 1;
    }
}
