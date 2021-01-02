/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents.models;

public class ReportCount {

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

    public String getResourceSimpleClassNameAndResourceName() {
        return resourceSimpleClassNameAndResourceName;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setResourceSimpleClassNameAndResourceName(String resourceSimpleClassNameAndResourceName) {
        this.resourceSimpleClassNameAndResourceName = resourceSimpleClassNameAndResourceName;
    }

}
