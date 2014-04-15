package com.appdynamics.extensions.urlpinger.http;

/**
 * POJO for http config parameters.
 */
public class SimpleHttpConfig {

    private String proxyHost;
    private String proxyPort;
    private String proxyUser;
    private String proxyPassword;
    private Integer connectionTimeout;
    private Integer socketTimeout;

    private SimpleHttpConfig(Builder  builder){
        this.proxyHost = builder.proxyHost;
        this.proxyPort = builder.proxyPort;
        this.proxyUser = builder.proxyUser;
        this.proxyPassword = builder.proxyPassword;
        this.connectionTimeout = builder.connectionTimeout;
        this.socketTimeout = builder.socketTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public static class Builder{
        private String proxyHost;
        private String proxyPort;
        private String proxyUser;
        private String proxyPassword;
        private Integer connectionTimeout;
        private Integer socketTimeout;

        public Builder setProxyHost(String proxyHost) {
            this.proxyHost = proxyHost;
            return this;
        }

        public Builder setProxyUser(String proxyUser) {
            this.proxyUser = proxyUser;
            return this;
        }

        public Builder setProxyPort(String proxyPort) {
            this.proxyPort = proxyPort;
            return this;
        }

        public Builder setProxyPassword(String proxyPassword) {
            this.proxyPassword = proxyPassword;
            return this;
        }

        public Builder setConnectionTimeout(int connectionTimeout){
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder setSocketTimeout(int socketTimeout){
            this.socketTimeout = socketTimeout;
            return this;
        }

        public SimpleHttpConfig build(){
            return new SimpleHttpConfig(this);
        }
    }
}
