/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import com.foilen.infra.ui.repositories.documents.models.ReportCount;
import com.foilen.infra.ui.repositories.documents.models.ReportTime;

@Document
public class ReportExecution {

    @Id
    private String txId;
    @Version
    private long version;

    private Date timestamp = new Date();
    private boolean success;

    private List<ReportCount> reportCounts = new ArrayList<>();
    private List<ReportTime> reportTimes = new ArrayList<>();

    public ReportExecution() {
    }

    public ReportExecution(String txId, boolean success) {
        this.txId = txId;
        this.success = success;
    }

    public List<ReportCount> getReportCounts() {
        return reportCounts;
    }

    public List<ReportTime> getReportTimes() {
        return reportTimes;
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

    public void setReportCounts(List<ReportCount> reportCounts) {
        this.reportCounts = reportCounts;
    }

    public void setReportTimes(List<ReportTime> reportTimes) {
        this.reportTimes = reportTimes;
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
