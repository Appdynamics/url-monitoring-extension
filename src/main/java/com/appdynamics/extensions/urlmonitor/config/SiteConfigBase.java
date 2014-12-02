package com.appdynamics.extensions.urlmonitor.config;

public class SiteConfigBase
{
    String method;
    int socketTimeout = -1;
    int connectTimeout = -1;
    Boolean treatAuthFailedAsError;
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

    public int getNumAttempts()
    {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts)
    {
        this.numAttempts = numAttempts;
    }

    public Boolean isTreatAuthFailedAsError()
    {
        return treatAuthFailedAsError;
    }

    public void setTreatAuthFailedAsError(Boolean treatAuthFailedAsError)
    {
        this.treatAuthFailedAsError = treatAuthFailedAsError;
    }

    @Override
    public String toString()
    {
        return "method='" + method + '\'' +
                ", socketTimeout=" + socketTimeout +
                ", connectTimeout=" + connectTimeout +
                ", numAttempts=" + numAttempts;
    }
}
