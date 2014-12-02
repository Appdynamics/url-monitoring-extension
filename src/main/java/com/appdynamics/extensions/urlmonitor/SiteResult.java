package com.appdynamics.extensions.urlmonitor;


public class SiteResult
{
    public enum ResultStatus
    {
        UNKNOWN,
        CANCELED,
        FAILED,
        ERROR,
        SUCCESS
    }

    private long elapsedTime;
    private ResultStatus status;
    private int responseCode;
    private long responseBytes;

    public SiteResult() {}

    public SiteResult(long elapsedTime, ResultStatus status, int responseCode, long responseBytes)
    {
        this.elapsedTime = elapsedTime;
        this.status = status;
        this.responseCode = responseCode;
        this.responseBytes = responseBytes;
    }

    public long getElapsedTime()
    {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime)
    {
        this.elapsedTime = elapsedTime;
    }

    public ResultStatus getStatus()
    {
        return status;
    }

    public void setStatus(ResultStatus status)
    {
        this.status = status;
    }

    public int getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    public long getResponseBytes()
    {
        return responseBytes;
    }

    public void setResponseBytes(long responseBytes)
    {
        this.responseBytes = responseBytes;
    }
}
