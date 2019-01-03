/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.audit;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * Auditing.
 */
@Entity
public class AuditItem implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date timestamp = new Date();
    @Column(nullable = false)
    private String txId;
    private boolean explicitChange = true;

    @Column(nullable = false, length = 50)
    private String type;
    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false)
    private String userType;
    @Column
    private String userName;

    @Column
    private String resourceFirstType;
    @Lob
    private String resourceFirst;
    @Column
    private String resourceSecondType;
    @Lob
    private String resourceSecond;

    @Column
    private String linkType;
    @Column
    private String tagName;

    public AuditItem() {
    }

    public String getAction() {
        return action;
    }

    public Long getId() {
        return id;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getResourceFirst() {
        return resourceFirst;
    }

    public String getResourceFirstType() {
        return resourceFirstType;
    }

    public String getResourceSecond() {
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

    public void setAction(String action) {
        this.action = action;
    }

    public void setExplicitChange(boolean explicitChange) {
        this.explicitChange = explicitChange;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public void setResourceFirst(String resourceFirst) {
        this.resourceFirst = resourceFirst;
    }

    public void setResourceFirstType(String resourceFirstType) {
        this.resourceFirstType = resourceFirstType;
    }

    public void setResourceSecond(String resourceSecond) {
        this.resourceSecond = resourceSecond;
    }

    public void setResourceSecondType(String resourceSecondType) {
        this.resourceSecondType = resourceSecondType;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

}
