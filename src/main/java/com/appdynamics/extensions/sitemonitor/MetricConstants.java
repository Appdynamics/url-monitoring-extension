/**
 * Copyright 2014 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.appdynamics.extensions.sitemonitor;

/**
 * Url Metric Constants.
 */
public class MetricConstants {
    public static final String CODE = "Code";
    public static final String RESPONSE_TIME = "ResponseTimeInMs";
    public static final String METRIC_SEPARATOR = "|";
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics" + METRIC_SEPARATOR + "SiteMonitor" + METRIC_SEPARATOR ;
    public static final int DEFAULT_STATUS_CODE = -1;
    public static final String EXTENSION_PREFIX = "[AppDExt-SiteMonitor] :: ";
}
