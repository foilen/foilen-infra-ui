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

public class ChartBuilder {

    private List<ChartSerieBuilder> serieBuilders = new ArrayList<>();
    private List<String> xLables = new ArrayList<>();

    public ChartSerieBuilder addSerie(String serieName) {
        ChartSerieBuilder chartSerieBuilder = new ChartSerieBuilder();
        chartSerieBuilder.setName(serieName);
        serieBuilders.add(chartSerieBuilder);
        return chartSerieBuilder;
    }

    public ChartBuilder addX(String xLabel) {
        xLables.add(xLabel);
        return this;
    }

    public Chart built() {

        Chart chart = new Chart();

        List<ChartOptionsScalesAxe> yAxes = chart.getOptions().getScales().getyAxes();
        ChartData data = chart.getData();

        // Set the x
        List<String> labels = data.getLabels();
        for (String xLable : xLables) {
            labels.add(xLable);
        }

        for (ChartSerieBuilder serieBuilder : serieBuilders) {
            // Set the max
            ChartOptionsScalesAxe chartOptionsScalesAxe = new ChartOptionsScalesAxe();
            chartOptionsScalesAxe.getTicks().setMax(serieBuilder.getMax());
            yAxes.add(chartOptionsScalesAxe);

            // Serie
            List<ChartDataDataSet> datasets = data.getDatasets();
            ChartDataDataSet chartDataDataSet = new ChartDataDataSet();
            chartDataDataSet.setLabel(serieBuilder.getName());
            chartDataDataSet.setBackgroundColor(serieBuilder.getBackgroundColor());
            chartDataDataSet.setBorderColor(serieBuilder.getColor());
            datasets.add(chartDataDataSet);
            List<Object> yPoints = chartDataDataSet.getData();

            // Set the y
            for (Double yValue : serieBuilder.getyValues()) {
                yPoints.add(yValue);
            }
        }

        return chart;
    }
}
