/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents.models;

public class ReportTime {

    private String updateEventHandlerSimpleClassName;
    private long timeInMs;

    public ReportTime() {
    }

    public ReportTime(String updateEventHandlerSimpleClassName, long timeInMs) {
        this.updateEventHandlerSimpleClassName = updateEventHandlerSimpleClassName;
        this.timeInMs = timeInMs;
    }

    public long getTimeInMs() {
        return timeInMs;
    }

    public String getUpdateEventHandlerSimpleClassName() {
        return updateEventHandlerSimpleClassName;
    }

    public void setTimeInMs(long timeInMs) {
        this.timeInMs = timeInMs;
    }

    public void setUpdateEventHandlerSimpleClassName(String updateEventHandlerSimpleClassName) {
        this.updateEventHandlerSimpleClassName = updateEventHandlerSimpleClassName;
    }

}
