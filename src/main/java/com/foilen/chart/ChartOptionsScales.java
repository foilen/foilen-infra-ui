/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartOptionsScales {

    private List<ChartOptionsScalesAxe> yAxes = new ArrayList<>();

    public List<ChartOptionsScalesAxe> getyAxes() {
        return yAxes;
    }

    public void setyAxes(List<ChartOptionsScalesAxe> yAxes) {
        this.yAxes = yAxes;
    }

}
