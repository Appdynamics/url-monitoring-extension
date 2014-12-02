package com.appdynamics.extensions.urlmonitor.config;

public class SiteConfigBase
{
    String method;
    int socketTimeout = -1;
    int connectTimeout = -1;
    boolean redirectsAllowed = true;
    int maxRedirects = -1;
    int numAttempts = -1;

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public boolean isRedirectsAllowed()
    {
        return redirectsAllowed;
    }

    public void setRedirectsAllowed(boolean redirectsAllowed)
    {
        this.redirectsAllowed = redirectsAllowed;
    }

    public int getMaxRedirects()
    {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects)
    {
        this.maxRedirects = maxRedirects;
    }

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts)
    {
        this.numAttempts = numAttempts;
    }

    @Override
    public String toString()
    {
        return "method='" + method + '\'' +
               ", socketTimeout=" + socketTimeout +
               ", connectTimeout=" + connectTimeout +
               ", redirectsAllowed=" + redirectsAllowed +
               ", maxRedirects=" + maxRedirects +
               ", numAttempts=" + numAttempts;
    }
}
