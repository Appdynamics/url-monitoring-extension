/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class DefaultSiteConfig extends SiteConfigBase {

    public DefaultSiteConfig()
    {
        method = "HEAD";
        socketTimeout = 30000;
        connectTimeout = 30000;
        treatAuthFailedAsError = true;
        numAttempts = 1;
    }
}
