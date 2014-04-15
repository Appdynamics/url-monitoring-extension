package com.appdynamics.extensions.urlpinger.common;

import com.appdynamics.extensions.urlpinger.MetricConstants;
import com.google.common.base.Strings;

import java.util.Map;

/**
 * Created by kunal.gupta on 4/13/14.
 */
public class MonitorArgs {

    private String proxyHost;
    private String proxyPort;
    private String proxyUser;
    private String proxyPassword;
    private String metricPrefix;

    public static final String PROXY_HOST = "proxy-host";
    public static final String PROXY_PORT = "proxy-port";
    public static final String PROXY_USER = "proxy-username";
    public static final String PROXY_PASSWORD = "proxy-password";
    public static final String METRIC_PREFIX = "metric-prefix";

    public MonitorArgs(Map<String, String> taskArgs){
        setMetricPrefix(MetricConstants.DEFAULT_METRIC_PREFIX);
        if(taskArgs != null){
            setProxyHost(taskArgs.get(PROXY_HOST));
            setProxyPort(taskArgs.get(PROXY_PORT));
            setProxyUser(taskArgs.get(PROXY_USER));
            setProxyPassword(taskArgs.get(PROXY_PASSWORD));
            if(!Strings.isNullOrEmpty(taskArgs.get(METRIC_PREFIX))){
                setMetricPrefix(taskArgs.get(METRIC_PREFIX));
            }
        }
    }


    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    @Override
    public String toString() {
        return "MonitorArgs{" +
                "proxyHost='" + proxyHost + '\'' +
                ", proxyPort='" + proxyPort + '\'' +
                ", proxyUser='" + proxyUser + '\'' +
                ", proxyPassword='" + proxyPassword + '\'' +
                ", metricPrefix='" + metricPrefix + '\'' +
                '}';
    }
}
