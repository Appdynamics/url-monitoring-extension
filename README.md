URL Monitor for AppDynamics
===========================

This extension works only with the standalone machine agent. It has been tested against various URLs with different authentication mechanisms.

# Use Case

The URL monitoring extension gathers metrics and sends them to the AppDynamics Metric Browser.

## Pre-requisites
Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met.

## Installation

1. To build from source, clone this repository and run 'mvn clean install'. This will produce a UrlMonitor-VERSION.zip in the target directory. Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/url-monitoring-extension/releases)
2. Unzip URLMonitor.zip and copy the 'UrlMonitor' directory to `<MACHINE_AGENT_HOME>/monitors/`
3. Configure the extension by referring to the below section.
4. Restart the Machine Agent. 
 
In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | URLMonitor (or the custom path you specified).

## Configuration

Every AppDynamics extension has a `monitor.xml` file that configures the extension. In this case, the `monitor.xml`
for this extension just has a single option: the path where the extension can find the main `config.yml` file. 
Note that the path is relative to `$AGENT_HOME`.

``` xml
    <task-arguments>
      <argument name="config-file" is-required="true" default-value="monitors/UrlMonitor/config.yml" />
    </task-arguments>
```

The main configuration for this extension then lives in a file called `config.yaml`. It uses a simple syntax that anyone can edit with a simple text editor. 
**Note: Please avoid using tab (\t) when editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).**

Here's a sample:

``` yml
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

### Examples

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

### Configuration Reference

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

#### ProxyConfig section
| Option Name                | Default Value | Mandatory| Option Description |
| :------------------------- | :------------ | :------- | :----------------- |
| **host**                   | none          | Yes(if proxy config specified)       | proxy host         |
| **port**                   | none          | Yes(if proxy config specified)       | proxy port         |
| **username**               | none          | Yes(if proxy config specified)       | proxy username     |
| **password**               | none          | Yes(if proxy config specified)       | proxy password     |

#### Auth Type

| Option Name                | Default Value | Mandatory | Option Description |
| :---------- | :------------ | :------------ | :----------------- |
| **authType**    | NONE         | Yes(if authType is specified)         | Name of the authentication type: BASIC, NTLM, ClientCert |
| **username**| null          | Yes(if authType is specified)          | username|
| **password**| null          | Yes(if authType is specified)          | password  |
| **encryptedPassword**| none          | no | encrypted password if using password ecryption |
| **encryptionKey**| none          | no | the key used to encrypt the password |
| **keyStoreType**| none          | no          | keyStoreType, used only in Client Cert Auth |
| **keyStorePath**| none          | no          | path to keyStore file, used only in Client Cert Auth |
| **keyStorePassword**| none          | no      | keyStorePassword, used only in Client Cert Auth |
| **trustStorePath**| none          | no        | path to trustStore file, used only in Client Cert Auth |
| **trustStorePassword**| none          | no    | trustStorePassword, used only in Client Cert Auth |
| **usePreemptiveAuth**| false          | no    | true if preemptive authentication is required |


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

## Metrics Provided ##

In the AppDynamics Metric Browser, URL Monitor's metrics can be seen at: Application Infrastructure Performance | Tier-ID | Custom Metrics | URL Monitor

Following metrics are reported for each site: 

- Average Response time (ms) ->  The time after the request is sent until the first byte is received back.
- First Byte Time (ms) -> Time taken from the time the request build has started to receive the first response byte.
- Download Time (ms) -> Total time taken to receive the entire response from the URL.
- Response Bytes -> It represents the length of the response returned from the URL.
- Response Code -> It represents the HTTP status code returned from the URL.
- Status -> It represents whether the URL is FAILED(2), ERROR(3) or SUCCESS(4).
         Possible values are: UNKNOWN(0), FAILED(2), ERROR(3), SUCCESS(4)
- Responsive Count(Available at GroupName Level) -> Number of sites in a given group, that responded successfully.


## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

## Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

    1. Stop the running machine agent.
    2. Delete all existing logs under <MachineAgent>/logs.
    3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
        <logger name="com.singularity">
        <logger name="com.appdynamics">
    4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
    5. Attach the zipped <MachineAgent>/conf/* directory here.
    6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.

For any support related questions, you can also contact help@appdynamics.com.


## Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/url-monitoring-extension/).

## Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.1.0       |
|Controller Compatibility  |4.5 or Later|
|Machine Agent Version     |4.5.13+     |
|Last Update               |15/01/2021  |

