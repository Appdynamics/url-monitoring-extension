package com.appdynamics.extensions.urlmonitor.auth;

import com.ning.http.client.Realm;

public class BasicAuth {

    private String username;
    private String password;

    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Realm.RealmBuilder realmBuilderBase() {
        return new Realm.RealmBuilder()
                .setScheme(Realm.AuthScheme.BASIC)
                .setPrincipal(getUsername())
                .setPassword(getPassword());
    }
}
