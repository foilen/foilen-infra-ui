/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.chart;

import java.util.ArrayList;
import java.util.List;

public class ChartDataDataSet {

    private String label;
    private String borderColor;
    private String backgroundColor;
    private List<Object> data = new ArrayList<>();

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public List<Object> getData() {
        return data;
    }

    public String getLabel() {
        return label;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
