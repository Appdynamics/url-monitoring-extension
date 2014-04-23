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

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.sitemonitor.jaxb.Site;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

/**
 * Unit of task performed by the UrlPinger monitoring extension.
 */

public class SiteMonitorTask implements Callable<SiteMonitorMetrics> {

    private SimpleHttpClient httpClient;
    private Site site;

    public static final Logger logger = Logger.getLogger(SiteMonitorTask.class);

    public SiteMonitorTask(SimpleHttpClient httpClient, Site site){
        this.httpClient = httpClient;
        this.site = site;
    }


    public SiteMonitorMetrics call() throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            Response response = httpClient.target(site.getUrl()).get();
            long responseTime = System.currentTimeMillis() - startTime;
            if (response != null) {
                return new SiteMonitorMetrics(site.getDisplayName(), response.getStatus(),responseTime);
            }
        }catch(Exception e){
            logger.error("[AppDExt::] Error in executing http request " ,e);
        }
        return new SiteMonitorMetrics(site.getDisplayName(), MetricConstants.DEFAULT_STATUS_CODE,-1l);
    }

}
