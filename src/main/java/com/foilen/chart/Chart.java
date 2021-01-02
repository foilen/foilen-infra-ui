/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

public class Chart {

    private String type = "line";
    private ChartData data = new ChartData();
    private ChartOptions options = new ChartOptions();

    public ChartData getData() {
        return data;
    }

    public ChartOptions getOptions() {
        return options;
    }

    public String getType() {
        return type;
    }

    public void setData(ChartData data) {
        this.data = data;
    }

    public void setOptions(ChartOptions options) {
        this.options = options;
    }

    public void setType(String type) {
        this.type = type;
    }

}
