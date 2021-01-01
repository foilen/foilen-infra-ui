/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChartSerieBuilder {

    private String name;
    private Double max;
    private List<Double> yValues = new ArrayList<>();
    private String color;
    private String backgroundColor;

    public ChartSerieBuilder addY(Double yValue) {
        yValues.add(yValue);
        return this;
    }

    public ChartSerieBuilder addY(Double... yValues) {
        this.yValues.addAll(Arrays.asList(yValues));
        return this;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getColor() {
        return color;
    }

    public Double getMax() {
        return max;
    }

    public String getName() {
        return name;
    }

    public List<Double> getyValues() {
        return yValues;
    }

    public ChartSerieBuilder setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ChartSerieBuilder setColor(String color) {
        this.color = color;
        return this;
    }

    public ChartSerieBuilder setMax(Double max) {
        this.max = max;
        return this;
    }

    public ChartSerieBuilder setName(String name) {
        this.name = name;
        return this;
    }

}
