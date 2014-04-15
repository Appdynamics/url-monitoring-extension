package com.appdynamics.extensions.urlpinger.jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by kunal.gupta on 4/10/14.
 */

@XmlRootElement(name = "monitor-urls")
@XmlAccessorType(XmlAccessType.FIELD)
public class MonitorUrls {

    @XmlElement(name="monitor-url",type = MonitorUrl.class)
    private List<MonitorUrl> monitorUrls;

    public List<MonitorUrl> getMonitorUrls() {
        return monitorUrls;
    }

    public void setMonitorUrls(List<MonitorUrl> monitorUrls) {
        this.monitorUrls = monitorUrls;
    }
}
