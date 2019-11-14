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
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Auditing.
 */
@Entity
@Table(indexes = { //
        @Index(name = "audit_item_action_id", columnList = "action, id desc"), //
        @Index(name = "audit_item_link_type_id", columnList = "linkType, id desc"), //
        @Index(name = "audit_item_resource_first_type_id", columnList = "resourceFirstType, id desc"), //
        @Index(name = "audit_item_resource_second_type_id", columnList = "resourceSecondType, id desc"), //
        @Index(name = "audit_item_tag_name_id", columnList = "tagName, id desc"), //
        @Index(name = "audit_item_timestamp_id", columnList = "timestamp, id desc"), //
        @Index(name = "audit_item_type_id", columnList = "type, id desc"), //
        @Index(name = "audit_item_tx_id_id", columnList = "txId, id desc"), //
        @Index(name = "audit_item_user_name_id", columnList = "userName, id desc"), //
        @Index(name = "audit_item_user_type_id", columnList = "userType, id desc"), //
})
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

    public AuditItem setAction(String action) {
        this.action = action;
        return this;
    }

    public AuditItem setExplicitChange(boolean explicitChange) {
        this.explicitChange = explicitChange;
        return this;
    }

    public AuditItem setId(Long id) {
        this.id = id;
        return this;
    }

    public AuditItem setLinkType(String linkType) {
        this.linkType = linkType;
        return this;
    }

    public AuditItem setResourceFirst(String resourceFirst) {
        this.resourceFirst = resourceFirst;
        return this;
    }

    public AuditItem setResourceFirstType(String resourceFirstType) {
        this.resourceFirstType = resourceFirstType;
        return this;
    }

    public AuditItem setResourceSecond(String resourceSecond) {
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
