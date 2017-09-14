/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.chart;

import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

public class ChartBuilderTest {

    @Test
    public void testTemperature() {
        ChartBuilder builder = new ChartBuilder();

        ChartSerieBuilder min = builder.addSerie("Min").setMax(40d);
        ChartSerieBuilder max = builder.addSerie("Max").setMax(50d);

        builder.addX("Monday").addX("Tuesday").addX("Wednesday").addX("Thursday");
        min.addY(17d).addY(28d).addY(35d).addY(32d);
        max.addY(30d, 31d, 32d, 33d);

        AssertTools.assertJsonComparison("ChartBuilderTest-testTemperature-expected.json", this.getClass(), builder.built());
    }

}
