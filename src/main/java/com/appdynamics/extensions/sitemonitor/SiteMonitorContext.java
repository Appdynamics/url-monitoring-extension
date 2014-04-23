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


import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.sitemonitor.jaxb.JAXBProvider;
import com.appdynamics.extensions.sitemonitor.jaxb.SiteConfig;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.Map;


public class SiteMonitorContext {

    public static final int DEFAULT_TIMEOUT_MS = 10000;
    public static final String CONFIG_ARG = "config-file";

    //provider to unmarshall the monitor-urls.xml
    final JAXBProvider jaxbProvider = new JAXBProvider(SiteConfig.class);

    //wrapper for httpClient
    final SimpleHttpClient simpleHttpClient;

    final SiteConfig siteConfig;

    public SiteMonitorContext(Map<String, String> taskArgs) throws JAXBException {
        siteConfig = (SiteConfig)jaxbProvider.unmarshal(getConfigFilename(taskArgs.get(CONFIG_ARG)));
        int socketTimeout = (int)((siteConfig.getSockTimeout() <= 0) ? DEFAULT_TIMEOUT_MS : siteConfig.getSockTimeout());
        int connTimeout = (int)((siteConfig.getConnTimeout() <= 0) ? DEFAULT_TIMEOUT_MS : siteConfig.getConnTimeout());
        simpleHttpClient = SimpleHttpClient.builder(taskArgs)
                .socketTimeout(socketTimeout)
                .connectionTimeout(connTimeout)
                .build();
    }

    public SiteConfig getSiteConfig() {
        return siteConfig;
    }

    public String getConfigFilename(String filename) {
        if(filename == null){
            return "";
        }
        //for absolute paths
        if(new File(filename).exists()){
            return filename;
        }
        //for relative paths. jarPath corresponds to machine agent jar
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if(!Strings.isNullOrEmpty(filename)){
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    public SimpleHttpClient getSimpleHttpClient() {
        return simpleHttpClient;
    }
}
