/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartData {

    private List<String> labels = new ArrayList<>();
    private List<ChartDataDataSet> datasets = new ArrayList<>();

    public List<ChartDataDataSet> getDatasets() {
        return datasets;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setDatasets(List<ChartDataDataSet> datasets) {
        this.datasets = datasets;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

}
