# AppDynamics Extensions URL Monitor CHANGELOG

### Version 2.0.0
- Moved to appd-exts-commons 2.2.3
- Configurable metrics
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