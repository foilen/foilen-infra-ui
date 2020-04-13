/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.foilen.login.api.LoginConfigDetails;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InfraUiConfig {

    // UI
    private String baseUrl;
    private long infiniteLoopTimeoutInMs = 120000;

    // MySql
    @Nullable
    private String mysqlHostName;
    private int mysqlPort = 3306;
    private String mysqlDatabaseName = "foilen_infra";
    @Nullable
    private String mysqlDatabaseUserName;
    @Nullable
    private String mysqlDatabasePassword;

    // Mongo
    private String mongoUri;

    // Email server
    private String mailHost = "127.0.0.1";
    private int mailPort = 25;
    @Nullable
    private String mailUsername;
    @Nullable
    private String mailPassword;

    // Email the Infra sends
    private String mailFrom;
    private String mailAlertsTo;

    // Login
    private LoginConfigDetails loginConfigDetails = new LoginConfigDetails();
    private String loginCookieSignatureSalt;

    // Security
    private String csrfSalt;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getCsrfSalt() {
        return csrfSalt;
    }

    public long getInfiniteLoopTimeoutInMs() {
        return infiniteLoopTimeoutInMs;
    }

    public LoginConfigDetails getLoginConfigDetails() {
        return loginConfigDetails;
    }

    public String getLoginCookieSignatureSalt() {
        return loginCookieSignatureSalt;
    }

    public String getMailAlertsTo() {
        return mailAlertsTo;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public String getMailHost() {
        return mailHost;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public int getMailPort() {
        return mailPort;
    }

    public String getMailUsername() {
        return mailUsername;
    }

    public String getMongoUri() {
        return mongoUri;
    }

    public String getMysqlDatabaseName() {
        return mysqlDatabaseName;
    }

    public String getMysqlDatabasePassword() {
        return mysqlDatabasePassword;
    }

    public String getMysqlDatabaseUserName() {
        return mysqlDatabaseUserName;
    }

    public String getMysqlHostName() {
        return mysqlHostName;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setCsrfSalt(String csrfSalt) {
        this.csrfSalt = csrfSalt;
    }

    public void setInfiniteLoopTimeoutInMs(long infiniteLoopTimeoutInMs) {
        this.infiniteLoopTimeoutInMs = infiniteLoopTimeoutInMs;
    }

    public void setLoginConfigDetails(LoginConfigDetails loginConfigDetails) {
        this.loginConfigDetails = loginConfigDetails;
    }

    public void setLoginCookieSignatureSalt(String loginCookieSignatureSalt) {
        this.loginCookieSignatureSalt = loginCookieSignatureSalt;
    }

    public void setMailAlertsTo(String mailAlertsTo) {
        this.mailAlertsTo = mailAlertsTo;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public void setMailUsername(String mailUsername) {
        this.mailUsername = mailUsername;
    }

    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public void setMysqlDatabaseName(String mysqlDatabaseName) {
        this.mysqlDatabaseName = mysqlDatabaseName;
    }

    public void setMysqlDatabasePassword(String mysqlDatabasePassword) {
        this.mysqlDatabasePassword = mysqlDatabasePassword;
    }

    public void setMysqlDatabaseUserName(String mysqlDatabaseUserName) {
        this.mysqlDatabaseUserName = mysqlDatabaseUserName;
    }

    public void setMysqlHostName(String mysqlHostName) {
        this.mysqlHostName = mysqlHostName;
    }

    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort = mysqlPort;
    }

}
