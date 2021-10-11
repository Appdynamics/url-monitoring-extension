/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.auth;

import org.asynchttpclient.Realm;

public class BasicAuth {

    private String username;
    private String password;
    private String encryptedPassword;
    private String encryptionKey;
    private boolean usePreemptiveAuth;

    public BasicAuth(String username, String password, String encryptedPassword, String encryptionKey, boolean usePreemptiveAuth) {
        this.username = username;
        this.password = password;
        this.encryptedPassword = encryptedPassword;
        this.encryptionKey = encryptionKey;
        this.usePreemptiveAuth = usePreemptiveAuth;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public boolean getUsePreemptiveAuth() {
        return usePreemptiveAuth;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public Realm.Builder realmBuilderBase() {
        return new Realm.Builder(getUsername(),AuthSchemeFactory.getPassword(getPassword(),getEncryptedPassword(),getEncryptionKey()))
                .setScheme(Realm.AuthScheme.BASIC)
                .setUsePreemptiveAuth(getUsePreemptiveAuth());
    }
}
