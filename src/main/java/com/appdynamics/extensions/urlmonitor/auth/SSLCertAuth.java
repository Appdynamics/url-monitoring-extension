/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.auth;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.urlmonitor.config.ProxyConfig;
import com.google.common.base.Strings;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;

public class SSLCertAuth {

    private static final Logger LOG = ExtensionsLoggerFactory.getLogger(SSLCertAuth.class);

    public SslContext getSSLContext(String keyStoreName, String keyStoreType, String password) {
        KeyStore ks = getKeyStore(keyStoreName, password);
        KeyManagerFactory keyManagerFactory = null;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(keyStoreType);
            keyManagerFactory.init(ks, password.toCharArray());

            LOG.debug("Default Algorithms: "+Security.getProperty("ssl.KeyManagerFactory.algorithm"));
            LOG.debug("List of available KeyManagers: "+ Arrays.toString(keyManagerFactory.getKeyManagers()));
            LOG.debug("Key Store type used for building SSLContext: "+(keyManagerFactory.getKeyManagers()[0]));

            SslContext context = SslContextBuilder
                    .forClient()
                    .sslProvider(SslProvider.JDK)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .keyManager(keyManagerFactory.getKeyManagers()[0])
                    .build();

            return context;

        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (KeyStoreException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (UnrecoverableKeyException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private KeyStore getKeyStore(String keyStoreName, String password) {
        KeyStore ks = null;
        FileInputStream fis = null;
        try {
            ks = KeyStore.getInstance("JKS");
            char[] passwordArray = password!=null ? password.toCharArray() : new char[25];
            fis = new java.io.FileInputStream(keyStoreName);
            ks.load(fis, passwordArray);
            fis.close();

        } catch (CertificateException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (KeyStoreException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
        return ks;
    }


    private HttpsURLConnection createConnection(URL url, final ProxyConfig proxyConfig) throws IOException {
        HttpsURLConnection con = null;
        if (proxyConfig != null && !Strings.isNullOrEmpty(proxyConfig.getHost())) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()));
            con = (HttpsURLConnection) url.openConnection(proxy);

            if (!Strings.isNullOrEmpty(proxyConfig.getUsername()) && !Strings.isNullOrEmpty(proxyConfig.getPassword())) {
                Authenticator authenticator = new Authenticator() {

                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(proxyConfig.getUsername(),
                                proxyConfig.getPassword().toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }

        } else {
            con = (HttpsURLConnection) url.openConnection();
        }
        return con;
    }
}
