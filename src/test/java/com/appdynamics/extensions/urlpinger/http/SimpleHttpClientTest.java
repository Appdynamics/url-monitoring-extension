package com.appdynamics.extensions.urlpinger.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class SimpleHttpClientTest {

    SimpleHttpConfig.Builder simpleHttpConfigBuilder;

    @Before
    public void setUp(){
        simpleHttpConfigBuilder = new SimpleHttpConfig.Builder();
    }

    @Test
    public void cannotGetEmptyUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("");
        assertNull(response);
    }

    @Test(expected = UnknownHostException.class)
    public void throwsExceptionForInvalidUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("ebay.");
    }

    @Test
    public void canGetHttpsUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("https://www.appdynamics.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void canGetHttpUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("http://www.google.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void canGetNoProtocolUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("www.amazon.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void canGetNoWWWUrl() throws Exception {
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(simpleHttpConfigBuilder.build());
        HttpResponse response = simpleHttpClient.get("ebay.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void canGetThroughHttpsProxyWithoutCreds() throws Exception {
        SimpleHttpConfig config = simpleHttpConfigBuilder.setProxyHost("https://181.143.208.18")
                                    .build();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(config);
        HttpResponse response = simpleHttpClient.get("https://www.appdynamics.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test
    public void canGetThroughHttpProxyWithoutCreds() throws Exception {
        SimpleHttpConfig config = simpleHttpConfigBuilder.setProxyHost("http://221.10.40.234")
                .setProxyPort("843")
                .setSocketTimeout(60000)
                .build();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(config);
        HttpResponse response = simpleHttpClient.get("www.appdynamics.com");
        assertTrue(response != null);
        assertTrue(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
    }

    @Test(expected = ConnectTimeoutException.class)
    public void throwConnectionTimeout() throws Exception {
        SimpleHttpConfig config = simpleHttpConfigBuilder.setConnectionTimeout(10).build();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(config);
        HttpResponse response = simpleHttpClient.get("http://www.ebay.com");
    }

    @Test(expected = SocketTimeoutException.class)
    public void throwSocketTimeout() throws Exception {
        SimpleHttpConfig config = simpleHttpConfigBuilder.setSocketTimeout(10).build();
        SimpleHttpClient simpleHttpClient = new SimpleHttpClient(config);
        HttpResponse response = simpleHttpClient.get("http://www.ebay.com");
    }
}
