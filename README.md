url-pinger-monitoring-extension
===============================

An AppDynamics extension to be used with a stand alone Java machine agent to ping set of urls and report their status.

## Metrics Provided ##
  1. Http status code.
  2. Response time in ms.
  3. Response size in bytes.
  

## Installation ##

1. Download and unzip UrlPingerMonitor.zip from AppSphere.
2. Copy the UrlPingerMonitor directory to `<MACHINE_AGENT_HOME>/monitors`.
 

## Configuration ##
1. To configure the urls to monitor, edit the monitor-urls.xml file in `<MACHINE_AGENT_HOME>/monitors/UrlPingerMonitor/`. Below is the format 

  ```
  <monitor-urls>
      <monitor-url>
          <displayName>AppDynamics</displayName>
          <url>http://www.appdynamics.com</url>
      </monitor-url>
      <monitor-url>
          <displayName>Google</displayName>
          <url>https://www.google.com</url>
      </monitor-url>
      <monitor-url>
          <displayName>Amazon</displayName>
          <url>https://www.amazon.com</url>
      </monitor-url>
      <monitor-url>
          <displayName>eBay</displayName>
          <url>http://www.ebay.com</url>
      </monitor-url>
  </monitor-urls>
  ```

2. To configure proxy settings,timeouts,metric prefix or the path to the monitor-urls.xml file, edit the <task-arguments> in the monitor.xml file. Below is the sample

  ```
  <task-arguments>
      <!-- config file-->
      <argument name="config-file" is-required="true" default-value="monitors/UrlPingerMonitor/monitor-urls.xml" />
      
      <!-- proxy settings -->
      <argument name="proxy-host" is-required="false" default-value=""/>
      <argument name="proxy-port" is-required="false" default-value=""/>
      <argument name="proxy-username" is-required="false" default-value=""/>
      <argument name="proxy-password" is-required="false" default-value=""/>
      <argument name="proxy-use-ssl" is-required="false" default-value="false"/>
      
      <!-- time out settings in ms-->
      <argument name="conn-timeout" is-required="false" default-value="10000"/>
      <argument name="sock-timeout" is-required="false" default-value="10000"/>
      
      <argument name="metric-prefix" is-required="false" default-value="Custom Metrics|UrlPinger|"/>
  </task-arguments>
   
  ```

## Custom Dashboard ##


## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [Community][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0  
**Controller Compatibility:** 3.7 or later

[GitHub]: https://github.com/Appdynamics/url-monitoring-extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com
