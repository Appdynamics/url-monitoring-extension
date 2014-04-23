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

package com.appdynamics.extensions.sitemonitor.jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlRootElement(name = "site-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiteConfig {

    @XmlElementWrapper(name="sites")
    @XmlElement(name="site",type = Site.class)
    private List<Site> sites;
    private long connTimeout;
    private long sockTimeout;
    private String metricPrefix;

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    public long getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(long connTimeout) {
        this.connTimeout = connTimeout;
    }

    public long getSockTimeout() {
        return sockTimeout;
    }

    public void setSockTimeout(long sockTimeout) {
        this.sockTimeout = sockTimeout;
    }

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public void setMetricPrefix(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }
}
