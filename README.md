site-monitoring-extension
===============================

An AppDynamics extension to be used with a stand alone Java machine agent to call set of urls and report their status.

## Metrics Provided ##
  1. Http status code.
  2. Response time in ms.


## Installation ##

1. Download and unzip SiteMonitor.zip from AppSphere.
2. Copy the SiteMonitor directory to `<MACHINE_AGENT_HOME>/monitors`.
 

## Configuration ##
1. To configure the urls to monitor, edit the site-config.xml file in `<MACHINE_AGENT_HOME>/monitors/SiteMonitor/`. Below is the format

  ```
<site-config>
    <sites>
        <site>
            <displayName>AppDynamics</displayName>
            <url>https://www.appdynamics.com</url>
        </site>
        <site>
            <displayName>Google</displayName>
            <url>https://www.google.com</url>
        </site>
        <site>
            <displayName>Amazon</displayName>
            <url>https://www.amazon.com</url>
        </site>
        <site>
            <displayName>eBay</displayName>
            <url>https://www.ebay.com</url>
        </site>
        <site>
            <url>http://facebook.com</url>
        </site>
        <site>
            <displayName>Ning</displayName>
            <url>http://www.ning.com</url>
        </site>
        <site>
            <displayName>StackOverFlow</displayName>
            <url>http://www.stackoverflow.com</url>
        </site>
    </sites>
    <connTimeout>10000</connTimeout>
    <sockTimeout>10000</sockTimeout>
    <metricPrefix>Custom Metrics|SiteMonitor|</metricPrefix>
</site-config>
  ```

2. To configure proxy settings or the path to the site-config.xml file, edit the <task-arguments> in the monitor.xml file. Below is the sample

  ```
  <task-arguments>
      <!-- config file-->
      <argument name="config-file" is-required="true" default-value="monitors/SiteMonitor/site-config.xml" />

      <!-- proxy settings -->
      <argument name="proxy-host" is-required="false" default-value=""/>
      <argument name="proxy-port" is-required="false" default-value=""/>
      <argument name="proxy-username" is-required="false" default-value=""/>
      <argument name="proxy-password" is-required="false" default-value=""/>
      <argument name="proxy-use-ssl" is-required="false" default-value="false"/>

      <argument name="log-prefix" is-required="false" default-value="[SiteMonitorAppDExt] " />
  </task-arguments>
   
  ```

## Custom Dashboard ##
![](https://github.com/Appdynamics/site-monitoring-extension/raw/master/site-monitor-dashboard.png)

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [Community][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.1
**Controller Compatibility:** 3.7 or later

[GitHub]: https://github.com/Appdynamics/site-monitoring-extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com

