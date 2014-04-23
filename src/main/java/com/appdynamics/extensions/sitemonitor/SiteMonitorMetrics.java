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
 * Metrics for url pinger extension.
 */
public class SiteMonitorMetrics {
    private String displayName;
    private int statusCode;
    private long responseTimeInMs;

    public SiteMonitorMetrics(String displayName, int statusCode, long responseTimeInMs) {
        this.displayName = displayName;
        this.statusCode = statusCode;
        this.responseTimeInMs = responseTimeInMs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getResponseTimeInMs() {
        return responseTimeInMs;
    }

}
