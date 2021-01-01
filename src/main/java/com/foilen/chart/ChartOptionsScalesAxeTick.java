/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

public class ChartOptionsScalesAxeTick {

    private boolean beginAtZero = true;
    private Double max;

    public Double getMax() {
        return max;
    }

    public boolean isBeginAtZero() {
        return beginAtZero;
    }

    public void setBeginAtZero(boolean beginAtZero) {
        this.beginAtZero = beginAtZero;
    }

    public void setMax(Double max) {
        this.max = max;
    }

}
