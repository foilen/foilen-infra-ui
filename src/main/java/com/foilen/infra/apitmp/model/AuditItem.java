/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.apitmp.model;

import java.util.Date;

import com.foilen.infra.api.model.AuditAction;
import com.foilen.infra.api.model.AuditType;
import com.foilen.infra.api.model.ResourceDetails;
import com.foilen.smalltools.restapi.model.AbstractApiBase;
import com.foilen.smalltools.tools.DateTools;

public class AuditItem extends AbstractApiBase {

    private long id;

    private Date timestamp;
    private String txId;
    private boolean explicitChange;

    private AuditType type;
    private AuditAction action;

    private String userType;
    private String userName;

    private ResourceDetails resourceFirst;
    private ResourceDetails resourceSecond;

    private String linkType;

    private String tagName;

    public AuditAction getAction() {
        return action;
    }

    public long getId() {
        return id;
    }

    public String getLinkType() {
        return linkType;
    }

    public ResourceDetails getResourceFirst() {
        return resourceFirst;
    }

    public ResourceDetails getResourceSecond() {
        return resourceSecond;
    }

    public String getTagName() {
        return tagName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTimestampText() {
        return DateTools.formatFull(timestamp);
    }

    public String getTxId() {
        return txId;
    }

    public AuditType getType() {
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

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public void setExplicitChange(boolean explicitChange) {
        this.explicitChange = explicitChange;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public void setResourceFirst(ResourceDetails resourceFirst) {
        this.resourceFirst = resourceFirst;
    }

    public void setResourceSecond(ResourceDetails resourceSecond) {
        this.resourceSecond = resourceSecond;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setType(AuditType type) {
        this.type = type;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

}
