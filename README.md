URL Monitor for AppDynamics
===========================

An AppDynamics Machine Agent extension to visit a set of URLs and report whether they are up or down.

This extension requires the Java Machine Agent.

## Installation ##

1. Download UrlMonitor.zip from the [Community][].
1. Copy UrlMonitor.zip into the directory where you installed the machine agent, under `$AGENT_HOME/monitors`.
1. Unzip the file. This will create a new directory called UrlMonitor.
1. In `$AGENT_HOME/monitors/UrlMonitor`, edit the file `monitor.xml` and configure the plugin.
1. Restart the machine agent.

## Configuration ##

The main configuration for this extension lives in a file called `config.yaml`. The file is designed to follow a simple format that anyone can edit. Here's a sample:

``` yaml
clientConfig:
    maxConnTotal:           1000
    maxConnPerRoute:        1000
    ignoreSslErrors:        true

defaultParams:
    method:                 HEAD
    socketTimeout:          30000
    connectTimeout:         30000
    numAttempts:            3
    treatAuthFailedAsError: true

sites:

- name:     Google
  url:      http://www.google.com

- name:     AppDynamics
  url:      https://www.appdynamics.com

- name:     Help
  url:      https://help.appdynamics.com

- name:     My Controller
  url:      https://mycontroller.saas.appdynamics.com/controller/rest/applications
  username: demouser@customer1
  password: foobar
```

Configuration in the `monitor.xml` for this extension consists of a single option: the path where the extension can find the aforementioned `config.yaml` file. Note that the path is relative to `$AGENT_HOME`.

``` xml
    <task-arguments>
      <argument name="config-file" is-required="true" default-value="monitors/UrlMonitor/config.yaml" />
    </task-arguments>
```

## Metrics Provided ##

- Average Response time (ms)
- Response Bytes
- Response Code
- Status : UNKNOWN(0), CANCELED(1), FAILED(2), ERROR(3), SUCCESS(4)


## Support

For any questions or feature requests, please contact the [AppDynamics Center of Excellence][].

**Version:** 1.0.3 
**Controller Compatibility:** 3.6 or later    
**Last Updated:** 11/17/2014  
**Author:** Todd Radel

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [Community][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

------------------------------------------------------------------------------

## Release Notes ##

### Version 1.0.3
 - Added new config option `treatAuthFailedAsError`. If false, then 401 errors will be expected and treated as
   "OK" result. 
 
### Version 1.0.2
 - Replaced Apache HTTP Component library with Ning Async HTTP Client.
 
### Version 1.0.1
 - Added support for self-signed SSL certificates.

### Version 1.0
 - Initial release to AppSphere.


[GitHub]: https://github.com/Appdynamics/url-monitoring-extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com


## Custom Dashboard ##
![](https://github.com/Appdynamics/site-monitoring-extension/raw/master/url-monitor-dashboard.png)

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [Community][].

## Support ##

For any questions or feature request, please contact [AppDynamics Center of Excellence][].

**Version:** 1.0.4
**Controller Compatibility:** 3.7 or later

[GitHub]: https://github.com/Appdynamics/url-monitoring-extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com

