/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

public class ChartOptions {

    private ChartOptionsScales scales = new ChartOptionsScales();

    public ChartOptionsScales getScales() {
        return scales;
    }

    public void setScales(ChartOptionsScales scales) {
        this.scales = scales;
    }

}
