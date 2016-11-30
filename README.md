URL Monitor for AppDynamics
===========================

An AppDynamics Machine Agent extension to visit a set of URLs and report whether they are up or down (and optionally 
whether certain text patterns appear on those pages).

This extension requires the Java Machine Agent.

## Installation ##

1. Download UrlMonitor.zip from the [AppDynamics Community][].
1. Copy UrlMonitor.zip into the directory where you installed the machine agent, under `$AGENT_HOME/monitors`.
1. Unzip the file. This will create a new directory called UrlMonitor.
1. In `$AGENT_HOME/monitors/UrlMonitor`, edit the configuration files (`monitor.xml` and `config.yaml`) 
   to configure the plugin.
1. Restart the machine agent.

## Configuration ##

Every AppDynamics extension has a `monitor.xml` file that configures the extension. In this case, the `monitor.xml`
for this extension just has a single option: the path where the extension can find the main `config.yaml` file. 
Note that the path is relative to `$AGENT_HOME`.

``` xml
    <task-arguments>
      <argument name="config-file" is-required="true" default-value="monitors/UrlMonitor/config.yaml" />
    </task-arguments>
```

The main configuration for this extension then lives in a file called `config.yaml`. It uses a simple syntax that anyone can edit with a simple text editor. Here's a sample:

``` yaml
clientConfig:
    maxConnTotal:             1000
    maxConnPerRoute:          1000
    ignoreSslErrors:          true
    userAgent:                Mozilla/5.0 AppDynamics-UrlMonitor/1.0.6

defaultParams:
    method:                   HEAD
    socketTimeout:            30000
    connectTimeout:           30000
    numAttempts:              3
    treatAuthFailedAsError:   true

sites:

- name:       Google
  url:        http://www.google.com

- name:       AppDynamics
  url:        https://www.appdynamics.com
  
- name:       My Slow Site
  url:        http://www.wordpress.com
  connectTimeout: 60000

- name:       Help
  url:        https://help.appdynamics.com
  proxyConfig:
      host: www.proxy.appdynamics.com
      port: 8080 

- name:       My Controller
  url:        https://mycontroller.saas.appdynamics.com/controller/rest/applications
  username:   demouser@customer1
  password:   welcome
  
- name:     My POST site
  url:      http://localhost:8293/api/v1/metrics
  username:
  password:
  connectTimeout: 60000
  method:   POST
  headers:
        Content-Type: application/json
  requestPayloadFile: src/test/resources/conf/postrequestPayloadFile
  matchPatterns:
     - name:       Error
       type:       substring
       pattern:    Error 400
```

### Examples ###

Increase the timeout threshold for a site that is often slow:

	- name:             My Slow Site
	  url:              http://www.wordpress.com
	  connectTimeout:   60000

Supply a username and password for HTTP Basic authentication:

	- name:            My Login Page
	  url:             http://localhost:8090/controller/rest/applications
	  username:        demouser@customer1
	  password:        welcome

Retrieve the Google home page and make sure the "I'm Feeling Lucky" button is visible:

	- name:           Google
	  url:            http://www.google.com
	  matchPatterns:
	    - name:       LuckyButton
	      type:       substring
	      pattern:    I'm Feeling Lucky

Retrieve the Google home page and count how many times the word "Mail" appears:

	- name:           Google
	  url:            http://www.google.com
	  matchPatterns:
	    - name:       MailCount
	      type:       word
	      pattern:    Mail

POST xml or json payload to any url and search for the patterns in the response

      - name:     My POST site
        url:      http://myposturl
        method:   POST
        headers:
              Content-Type: application/json
        requestPayloadFile: path/to/postrequestPayloadFile
        matchPatterns:
            - name:       Error
              type:       substring
              pattern:    Error 400

### Configuration Reference ###

#### Client Section

The **clientConfig** section sets options for the HTTP client library, including:

| Option Name         | Default Value | Option Description |
| :------------------ | :------------ | :----------------- |
| **maxConnTotal**    | 1000          | Maximum number of simultaneous HTTP connections |
| **maxConnPerRoute** | 1000          | Maximum number of simultaneous HTTP connections to a single host |
| **threadCount**     | 10            | Maximum number of Threads spawned to cater HTTP request
| **ignoreSSlErrors** | false         | Whether to ignore errors in SSL certificate validation or host validation |
| **userAgent**       | Mozilla/5.0 (compatible; AppDynamics UrlMonitor; http://www.appdynamics.com/) | Custom User-Agent header to send with requests (can be used to mimic desktop or mobile browsers) |
| **followRedirects** | true          | Whether the client should follow Redirect responses |
| **maxRedirects**    | 10            | Maximum redirects 

#### Default Site Section

The **defaultParams** section sets the default options for all sites. These options can then be overriden
at the individual site level.

| Option Name                | Default Value | Option Description |
| :------------------------- | :------------ | :----------------- |
| **method**                 | GET           | HTTP method to use (e.g. GET, POST, HEAD, OPTIONS, etc.). The default is "HEAD", which avoids the overhead of retrieving the entire body of the response, but which prevents the agent from doing pattern matching or reporting the response size. Make sure you set the method to GET if you want these features. |
| **socketTimeout**          | 30000         | Maximum time to wait for a socket connection to open, in milliseconds |
| **connectTimeout**         | 30000         | Maximum time to wait for the HTTP handshake, in milliseconds |
| **numAttempts**            | 1             | Number of times the site will be retrieved. The metrics then reported will be an average over all attempts. |
| **treatAuthFailedAsError** | true          | If **false**, the extension will report the site status as "SUCCESS" even if authentication fails. |
| **proxyConfig**            | null          | Specify the host and port of the proxy. |

#### ProxyConfig section
| Option Name                | Default Value | Option Description |
| :------------------------- | :------------ | :----------------- |
| **host**                   | none          | proxy host         |
| **port**                   | none          | proxy port         |
| **username**               | none          | proxy username     |
| **password**               | none          | proxy password     |

### Site Section

| Option Name                | Default Value | Option Description |
| :---------- | :------------ | :----------------- |
| **name**    | none          | Name of the url with which metric folder that will be created in Metric Browser |
| **url**     | none          | The url to monitor |
| **username**| none          | username if url has Basic Authentication |
| **password**| none          | password if url has Basic Authentication |
| **matchPatterns**| none          | match patterns to search for in the response |


##### Match Pattern Section

| Option Name | Default Value | Option Description |
| :---------- | :------------ | :----------------- |
| **name**    | none          | Name of the metric folder that will be created in Metric Browser |
| **pattern** | none          | The string to search for |
| **type**    | substring     | Can be one of: substring, caseInsensitiveSubstring, regex, or word (see below) |

The options for the pattern type are:

| Value | Meaning |
| :---- | :------ |
| substring | Exact match on the given string  
| caseInsensitiveSubstring | Case-insensitive match on the given string |
| regex | Regular expression match |
| word | Case-insensitive, but must be surrounded by non-word characters |

## Metrics Provided ##

- Average Response time (ms)
- Response Bytes
- Response Code
- Status : UNKNOWN(0), CANCELED(1), FAILED(2), ERROR(3), SUCCESS(4)


## Sample Custom Dashboard ##
![](https://github.com/Appdynamics/site-monitoring-extension/raw/master/url-monitor-dashboard.png)

## Support ##

For any questions or feature requests, please contact the [AppDynamics Center of Excellence][].

**Version:** 1.2.3  
**Controller Compatibility:** 3.7 or later    
**Last Updated:** 07/28/2016
**Author:** Todd Radel

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Community][].

------------------------------------------------------------------------------

## Release Notes ##

### Version 1.2.3
 - Fixed metric drop issue in case of large number of URLs

### Version 1.2.2
 - Corrected issues with ignoreSSLErrors functionality.

### Version 1.2.1
 - Added POST functionality.

### Version 1.1.0
 - Added pattern matching against the retrieved pages.

### Version 1.0.6
 - Version bump

### Version 1.0.5
 - Added metadata for new extension repository.

### Version 1.0.4
 - Rebranded as url-monitoring-extension.

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
[AppDynamics Community]: https://www.appdynamics.com/community/exchange/
[AppDynamics Center of Excellence]: mailto:help@appdynamics.com

