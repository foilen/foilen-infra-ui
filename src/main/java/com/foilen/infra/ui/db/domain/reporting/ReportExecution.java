/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.reporting;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class ReportExecution implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @Column(nullable = false)
    private Date timestamp = new Date();
    @Column(nullable = false)
    private String txId;
    private boolean success;

    public ReportExecution() {
    }

    public ReportExecution(String txId, boolean success) {
        this.txId = txId;
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTxId() {
        return txId;
    }

    public long getVersion() {
        return version;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
