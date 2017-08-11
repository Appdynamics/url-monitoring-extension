package com.appdynamics.extensions.urlmonitor.auth;

import com.appdynamics.extensions.urlmonitor.config.SiteConfig;
import com.ning.http.client.Realm;

/**
 * Factory to generate the RealmBuilder based on the chosen authType
 */
public class AuthSchemeFactory {

    public static Realm.RealmBuilder getAuth(AuthTypeEnum authType, SiteConfig siteConfig){

        Realm.RealmBuilder realmBuilder = null;
        switch (authType){
            case NTLM:
                NTLMAuth ntlmAuth = new NTLMAuth(siteConfig.getUsername(),siteConfig.getPassword(),siteConfig.getUrl());
                realmBuilder = ntlmAuth.realmBuilderBase();
                break;
            case BASIC:
                BasicAuth basicAuth = new BasicAuth(siteConfig.getUsername(),siteConfig.getPassword());
                realmBuilder = basicAuth.realmBuilderBase();
                break;
        }
        return realmBuilder;
    }
}
