/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor;

import com.appdynamics.extensions.urlmonitor.auth.AuthTypeEnum;
import com.appdynamics.extensions.urlmonitor.config.MonitorConfig;
import com.appdynamics.extensions.urlmonitor.config.RequestConfig;
import com.appdynamics.extensions.urlmonitor.config.SiteConfig;
import com.appdynamics.extensions.urlmonitor.httpClient.ClientFactory;
import com.ning.http.client.AsyncHttpClient;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class RequestConfigTest {

    RequestConfig requestConfig;
    RequestConfig requestConfigSpy;

    ClientFactory clientFactory;
    AsyncHttpClient asyncHttpClient;

    MonitorConfig monitorConfig;

    List<RequestConfig> requestConfigList = new ArrayList<RequestConfig>();

    @Mock
    SiteConfig[] siteConfigs;

    @Before
    public void init() throws Exception {

        requestConfig = Mockito.mock(RequestConfig.class);
        monitorConfig = Mockito.mock(MonitorConfig.class);
        requestConfigSpy = spy(new RequestConfig());

        clientFactory = Mockito.mock(ClientFactory.class);
        asyncHttpClient = Mockito.mock(AsyncHttpClient.class);

        when(clientFactory.createHttpClient(Mockito.eq(monitorConfig), Mockito.any(String.class), Mockito.any(SSLContext.class))).thenReturn(asyncHttpClient);

    }

    @Test
    public void testSetConfig() throws Exception {
        List<RequestConfig> requestConfigList = getTestRequestConfig();
        when(requestConfig.setClientForSite(monitorConfig, siteConfigs)).thenReturn(requestConfigList);

        Assert.assertEquals(requestConfig.setClientForSite(monitorConfig, siteConfigs).size(),2);
    }

    @Test(expected=TaskExecutionException.class)
    public void testNullMonitor() throws Exception {
        requestConfigSpy.setClientForSite(null, monitorConfig.getSites());
    }

    @Test
    public void testCreateClient() throws Exception {
        Assert.assertNotNull(clientFactory.createHttpClient(monitorConfig, AuthTypeEnum.NONE.name(),null));
    }

    @Test
    public void testCreateSSLCertClient() throws Exception {
        Assert.assertNotNull(clientFactory.createHttpClient(monitorConfig, AuthTypeEnum.NONE.name(),Mockito.mock(SSLContext.class)));
    }

    @Test
    public void testCloseClient(){
        doNothing().when(requestConfig).closeClients(requestConfigList);
        requestConfig.closeClients(getTestRequestConfig());
    }


    private List<RequestConfig> getTestRequestConfig(){
        requestConfigList.add(Mockito.mock(RequestConfig.class));
        requestConfigList.add(Mockito.mock(RequestConfig.class));

        return requestConfigList;
    }
}
