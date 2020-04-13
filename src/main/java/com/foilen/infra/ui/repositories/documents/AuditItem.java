/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Auditing.
 */
@Document
public class AuditItem {

    @Id
    private String id;

    private Date timestamp = new Date();

    private String txId;
    private boolean explicitChange = true;

    private String type;
    private String action;

    private String userType;
    private String userName;

    private String resourceFirstType;
    private Object resourceFirst;
    private String resourceSecondType;
    private Object resourceSecond;

    private String linkType;
    private String tagName;

    public AuditItem() {
    }

    public String getAction() {
        return action;
    }

    public String getId() {
        return id;
    }

    public String getLinkType() {
        return linkType;
    }

    public Object getResourceFirst() {
        return resourceFirst;
    }

    public String getResourceFirstType() {
        return resourceFirstType;
    }

    public Object getResourceSecond() {
        return resourceSecond;
    }

    public String getResourceSecondType() {
        return resourceSecondType;
    }

    public String getTagName() {
        return tagName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTxId() {
        return txId;
    }

    public String getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isExplicitChange() {
        return explicitChange;
    }

    public AuditItem setAction(String action) {
        this.action = action;
        return this;
    }

    public AuditItem setExplicitChange(boolean explicitChange) {
        this.explicitChange = explicitChange;
        return this;
    }

    public AuditItem setId(String id) {
        this.id = id;
        return this;
    }

    public AuditItem setLinkType(String linkType) {
        this.linkType = linkType;
        return this;
    }

    public AuditItem setResourceFirst(Object resourceFirst) {
        this.resourceFirst = resourceFirst;
        return this;
    }

    public AuditItem setResourceFirstType(String resourceFirstType) {
        this.resourceFirstType = resourceFirstType;
        return this;
    }

    public AuditItem setResourceSecond(Object resourceSecond) {
        this.resourceSecond = resourceSecond;
        return this;
    }

    public AuditItem setResourceSecondType(String resourceSecondType) {
        this.resourceSecondType = resourceSecondType;
        return this;
    }

    public AuditItem setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public AuditItem setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public AuditItem setTxId(String txId) {
        this.txId = txId;
        return this;
    }

    public AuditItem setType(String type) {
        this.type = type;
        return this;
    }

    public AuditItem setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public AuditItem setUserType(String userType) {
        this.userType = userType;
        return this;
    }

}
