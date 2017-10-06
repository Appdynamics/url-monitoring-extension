package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class ClientConfig
{
    private int maxConnTotal = 1000;
    private int maxConnPerRoute = 1000;
    private int threadCount = 10;
    private boolean ignoreSslErrors = false;
    private int maxRedirects = 10;
    private String userAgent = "Mozilla/5.0 (compatible; AppDynamics UrlMonitor; http://www.appdynamics.com/)";

    public int getThreadCount()
    {
        return threadCount;
    }

    public void setThreadCount(int threadCount)
    {
        this.threadCount = threadCount;
    }

    public int getMaxConnTotal()
    {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal)
    {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute()
    {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute)
    {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public boolean isIgnoreSslErrors()
    {
        return ignoreSslErrors;
    }

    public void setIgnoreSslErrors(boolean ignoreSslErrors)
    {
        this.ignoreSslErrors = ignoreSslErrors;
    }

    public int getMaxRedirects()
    {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects)
    {
        this.maxRedirects = maxRedirects;
    }

    public String getUserAgent() { return userAgent; }

    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }


    @Override
    public String toString()
    {
        return "maxConnTotal=" + maxConnTotal +
                ", maxConnPerRoute=" + maxConnPerRoute +
                ", ignoreSslErrors=" + ignoreSslErrors;
    }
}
