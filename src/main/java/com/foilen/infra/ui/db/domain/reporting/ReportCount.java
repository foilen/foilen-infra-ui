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
public class ReportCount extends AbstractBasics implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "reportExecutionId", nullable = false)
    private ReportExecution reportExecution;

    @Column(nullable = false)
    private String resourceSimpleClassNameAndResourceName;
    private int count;

    public ReportCount() {
    }

    public ReportCount(String resourceSimpleClassNameAndResourceName, int count) {
        this.resourceSimpleClassNameAndResourceName = resourceSimpleClassNameAndResourceName;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public Long getId() {
        return id;
    }

    public ReportExecution getReportExecution() {
        return reportExecution;
    }

    public String getResourceSimpleClassNameAndResourceName() {
        return resourceSimpleClassNameAndResourceName;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setReportExecution(ReportExecution reportExecution) {
        this.reportExecution = reportExecution;
    }

    public void setResourceSimpleClassNameAndResourceName(String resourceSimpleClassNameAndResourceName) {
        this.resourceSimpleClassNameAndResourceName = resourceSimpleClassNameAndResourceName;
    }

}
