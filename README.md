URL Monitor for AppDynamics
===========================

An AppDynamics Machine Agent extension to visit a set of URLs and report whether they are up or down (and optionally 
whether certain text patterns appear on those pages).

This extension requires the Java Machine Agent.

## Installation ##

1. Download and unzip UrlMonitor.zip from the [AppDynamics Community][].
1. Copy UrlMonitor directory where you installed the machine agent, under `$AGENT_HOME/monitors`.
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

The main configuration for this extension then lives in a file called `config.yaml`. It uses a simple syntax that anyone can edit with a simple text editor. 
**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

Here's a sample:

``` yaml
# Client level configurations, common across all sites to be monitored
clientConfig:
    maxConnTotal:    1000
    maxConnPerRoute: 1000
    maxRedirects: 10
    ignoreSslErrors: true
    userAgent:       Mozilla/5.0 (compatible; AppDynamics UrlMonitor; http://www.appdynamics.com/)

defaultParams:
    method:          GET
    socketTimeout:   30000
    connectTimeout:  30000
    numAttempts:     1


#Sites that need to be monitored
sites:

     #No authentication, with a pattern to match
   - name:     Google
     url:      http://www.google.com
     followRedirects: false
     groupName: MySites
     # Patterns to be matched, multiple patterns(to be matched) can be configured for a given site
     matchPatterns:
     - name: LuckyButton
       type: caseInsensitiveSubstring
       pattern: Google

   - name:     AppDynamics
     url:      http://www.appdynamics.com
     authType: BASIC

   - name:     File Download
     url:      https://github.com/Appdynamics/url-monitoring-extension/releases/download/1.0.6/UrlMonitor.zip

    # Basic Authentication with password encryption
   - name:       My Controller
     url:        https://mycontroller.saas.appdynamics.com/controller/rest/applications
     username:   demouser@customer1
     password:   welcome
     encryptedPassword: "IGVtC9eudmgG8RDjmRjGPQ=="
     encryptionKey: 
     authType: BASIC

     #NTLM Auth Sample Configuration
   - name:     My Controller
     url:      http://localhost:8090/controller
     username: user@DOMAIN
     password: password
     authType: NTLM
     connectTimeout: 60000

     # Client Cert Auth Sample Configuration
   - name:         LocalHost
     url:          https://localhost:8443
     password:     password
     authType:     SSL
     keyStoreType: SUNX509
     keyStorePath: /Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/bin/client.jks
     keyStorePassword: password
     trustStorePath: /Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/bin/client.jks
     trustStorePassword: password

     #POST request sample configuration
   - name:     My POST site
     url:      http://localhost:8293/api/v1/metrics
     username:
     password:
     connectTimeout: 60000
     method:   POST
     headers:
           Content-Type: application/json
     requestPayloadFile: src/test/resources/conf/postrequestPayloadFile.json
     matchPatterns:
       - name:       Error
         type:       substring
         pattern:    Error 400

     #Proxy Configuration
   - name:     Google
     url:      http://www.google.com
     groupName: MySites
     proxyConfig:
       host: ""
       port: ""
       username: ""
       password: ""

#prefix used to show up metrics in AppDynamics. This will create it in specific Tier. Replace
metricPrefix: Server|Component:<TierID>|Custom Metrics|URLMonitor|
#This will create this metric in all the tiers, under this path
#metricPrefix: Custom Metrics|URLMonitor|
```

### Examples ###

Increase the timeout threshold for a site that is often slow:

	- name:             My Slow Site
	  url:              http://www.wordpress.com
	  connectTimeout:   60000

Supply a username and password for HTTP Basic authentication:

	- name:            My Login Page
	  url:             http://localhost:8090/controller/rest/applications
          authtype:        BASIC
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
        requestPayloadFile: path/to/postrequestPayloadFile.json
        matchPatterns:
            - name:       Error
              type:       substring
              pattern:    Error 400

### Configuration Reference ###

#### Client Config

The **clientConfig** section sets options for the HTTP client library, including:

| Option Name         | Default Value | Mandatory| Option Description |
| :------------------ | :------------ | :------- | :----------------- |
| **maxConnTotal**    | 1000          | No       |Maximum number of simultaneous HTTP connections |
| **maxConnPerRoute** | 1000          | No       | Maximum number of simultaneous HTTP connections to a single host |
| **threadCount**     | 10            | No       | Maximum number of Threads spawned to cater HTTP request
| **ignoreSSlErrors** | false         | No       | Whether to ignore errors in SSL certificate validation or host validation |
| **userAgent**       | Mozilla/5.0 (compatible; AppDynamics UrlMonitor; http://www.appdynamics.com/) | No       | Custom User-Agent header to send with requests (can be used to mimic desktop or mobile browsers) |
| **maxRedirects**    | 10            | No       | Maximum redirects 

#### Default Params

The **defaultParams** section sets the default options for all sites. These options can then be overriden
at the individual site level.

| Option Name                | Default Value | Mandatory| Option Description |
| :------------------------- | :------------ | :--------| :----------------- |
| **method**                 | GET           | No       | HTTP method to use (e.g. GET, POST, HEAD, OPTIONS, etc.). The default is "HEAD", which avoids the overhead of retrieving the entire body of the response, but which prevents the agent from doing pattern matching or reporting the response size. Make sure you set the method to GET if you want these features. |
| **socketTimeout**          | 30000         | No       | Maximum time to wait for a socket connection to open, in milliseconds |
| **connectTimeout**         | 30000         | No       | Maximum time to wait for the HTTP handshake, in milliseconds |
| **numAttempts**            | 1             | No       | Number of times the site will be retrieved. The metrics then reported will be an average over all attempts. |
| **treatAuthFailedAsError** | true          | No       | If **false**, the extension will report the site status as "SUCCESS" even if authentication fails. |


#### ProxyConfig section
| Option Name                | Default Value | Mandatory| Option Description |
| :------------------------- | :------------ | :------- | :----------------- |
| **host**                   | none          | Yes(if proxy config specified)       | proxy host         |
| **port**                   | none          | Yes(if proxy config specified)       | proxy port         |
| **username**               | none          | Yes(if proxy config specified)       | proxy username     |
| **password**               | none          | Yes(if proxy config specified)       | proxy password     |

### Site Section

| Option Name                | Default Value | Mandatory| Option Description |
| :---------- | :------------ | :------- | :----------------- |
| **name**    | none          | Yes       | Name of the url with which metric folder that will be created in Metric Browser |
| **url**     | none          | Yes       | The url to monitor |
| **followRedirects** | true          | No       | Whether the site should follow Redirect responses |
| **groupName**     | none          | No       | The group under which site needs to be categorised |
| **authType**| none          | No       | type of authentication, supported auth are Basic, NTLM, Client Cert |
| **matchPatterns**| none          | No       | Matches the specified patterns in the URL response , and reports the total number of matches count as metric |
| **proxyConfig**            | null          | No       | Specify the host and port of the proxy. |
| **headers**            | none          | No       | Component of request header section, e.g.: Content-Type. |
| **requestPayloadFile**            | none          | No       | Payload file(XML or JSON) to upload to URL. |


#### Auth Type

| Option Name                | Default Value | Mandatory | Option Description |
| :---------- | :------------ | :------------ | :----------------- |
| **authType**    | NONE         | yes          | Name of the authentication type: BASIC, NTLM, ClientCert |
| **username**| null          | yes          | username|
| **password**| null          | yes          | password  |
| **encryptedPassword**| none          | no | encrypted password if using password ecryption |
| **encryptionKey**| none          | no | the key used to encrypt the password |
| **keyStoreType**| none          | no          | keyStoreType, used only in Client Cert Auth |
| **keyStorePath**| none          | no          | path to keyStore file, used only in Client Cert Auth |
| **keyStorePassword**| none          | no      | keyStorePassword, used only in Client Cert Auth |
| **trustStorePath**| none          | no        | path to trustStore file, used only in Client Cert Auth |
| **trustStorePassword**| none          | no    | trustStorePassword, used only in Client Cert Auth |


#### Match Pattern Section

| Option Name | Default Value | Mandatory | Option Description |
| :---------- | :------------ |:----------| :----------------- |
| **name**    | none          | Yes(if MatchPattern specified)       | Name of the metric folder that will be created in Metric Browser |
| **pattern** | none          | Yes(if MatchPattern specified)       | The string to search for |
| **type**    | substring     | Yes(if MatchPattern specified)       | Can be one of: substring, caseInsensitiveSubstring, regex, or word (see below) |

The options for the pattern type are:

| Value | Meaning |
| :---- | :------ |
| substring | Exact match on the given string  
| caseInsensitiveSubstring | Case-insensitive match on the given string |
| regex | Regular expression match |
| word | Case-insensitive, but must be surrounded by non-word characters |

Metrics for match pattern appears under the following path: 

 Site->Pattern Matches -> Name of MatchPattern(As specified in config.yml) -> Count


## Password Encryption Support ##

To avoid setting the clear text password in the config.yml, please follow the process to encrypt the password and set the encrypted password and the key in the config.yml

1. Download the util jar to encrypt the password from here
2. Encrypt password from the commandline
    java -cp "appd-exts-commons-1.1.2.jar" com.appdynamics.extensions.crypto.Encryptor myKey myPassword
3. These values should be used in the passwordEncrypted and encryptionKey fields in config.yml

## Metrics Provided ##

In the AppDynamics Metric Browser, URL Monitor's metrics can be seen at: Application Infrastructure Performance | Tier-ID | Custom Metrics | URL Monitor

Following metrics are reported for each site: 

- Average Response time (ms) ->  The time after the request is sent until the first byte is received back.
- First Byte Time (ms) -> Time taken from the time the request build has started to receive the first response byte.
- Download Time (ms) -> Total time taken to receive the entire response from the URL.
- Response Bytes -> It represents the length of the response returned from the URL.
- Response Code -> It represents the HTTP status code returned from the URL.
- Status -> It represents whether the URL is FAILED(2), ERROR(3) or SUCCESS(4).
         Possible values are: UNKNOWN(0), CANCELLED(1), FAILED(2), ERROR(3), SUCCESS(4)
- Responsive Count(Available at GroupName Level) -> Number of sites in a given group, that responded successfully.


## Sample Custom Dashboard ##
![](https://github.com/Appdynamics/site-monitoring-extension/raw/master/url-monitor-dashboard.png)

## Support ##

For any questions or feature requests, please contact the [AppDynamics Center of Excellence][].

**Version:** 1.2.6  
**Controller Compatibility:** 3.7 or later    
**Last Updated:** 09/13/2017
**Author:** Todd Radel

## Contributing ##

Always feel free to fork and contribute any changes directly via [GitHub][].

## Community ##

Find out more in the [AppDynamics Community][].

------------------------------------------------------------------------------

## Release Notes ##

### Version 1.2.6
 - Added support for Client Side Cert auth and password encryption

### Version 1.2.5
 - Added support for NTLM auth and ignoring SSL Cert errors

### Version 1.2.4
 - Adding groupName to group multiple sites

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

