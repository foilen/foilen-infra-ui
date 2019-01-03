/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.reporting;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.foilen.smalltools.tools.AbstractBasics;

@Entity
public class ReportTime extends AbstractBasics implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "reportExecutionId", nullable = false)
    private ReportExecution reportExecution;

    @Column(nullable = false)
    private String updateEventHandlerSimpleClassName;
    private long timeInMs;

    public ReportTime() {
    }

    public ReportTime(String updateEventHandlerSimpleClassName, long timeInMs) {
        this.updateEventHandlerSimpleClassName = updateEventHandlerSimpleClassName;
        this.timeInMs = timeInMs;
    }

    public Long getId() {
        return id;
    }

    public ReportExecution getReportExecution() {
        return reportExecution;
    }

    public long getTimeInMs() {
        return timeInMs;
    }

    public String getUpdateEventHandlerSimpleClassName() {
        return updateEventHandlerSimpleClassName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReportExecution(ReportExecution reportExecution) {
        this.reportExecution = reportExecution;
    }

    public void setTimeInMs(long timeInMs) {
        this.timeInMs = timeInMs;
    }

    public void setUpdateEventHandlerSimpleClassName(String updateEventHandlerSimpleClassName) {
        this.updateEventHandlerSimpleClassName = updateEventHandlerSimpleClassName;
    }

}
