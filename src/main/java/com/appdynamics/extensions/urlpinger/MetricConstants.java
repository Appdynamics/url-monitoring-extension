package com.appdynamics.extensions.urlpinger;

/**
 * Url Metric Constants.
 */
public class MetricConstants {
    public static final String CODE = "Code";
    public static final String RESPONSE_TIME = "ResponseTimeInMs";
    public static final String RESPONSE_SIZE_IN_BYTES = "ResponseSizeInBytes";
    public static final String METRIC_SEPARATOR = "|";
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics" + METRIC_SEPARATOR + "UrlPinger" + METRIC_SEPARATOR ;
    public static final int DEFAULT_STATUS_CODE = -1;
    public static final String EXTENSION_PREFIX = "[AppDExt-UrlPinger] :: ";
}
