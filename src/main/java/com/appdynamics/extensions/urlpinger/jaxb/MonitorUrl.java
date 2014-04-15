package com.appdynamics.extensions.urlpinger.jaxb;

import com.google.common.base.Strings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by kunal.gupta on 4/10/14.
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "monitor-url")
public class MonitorUrl {

    private String displayName;
    private String url;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(url) && !Strings.isNullOrEmpty(displayName);
    }
}
