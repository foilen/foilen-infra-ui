/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.chart.Chart;
import com.foilen.infra.api.model.SystemStats;

public interface MachineStatisticsService {

    void addStats(String machineName, List<SystemStats> systemStats);

    void aggregateByDay();

    void aggregateByHour();

    Chart getCpuChart(String machineName);

    Chart getDiskChart(String machineName);

    Chart getMemoryChart(String machineName);

    Chart getNetworkChart(String machineName);

}
