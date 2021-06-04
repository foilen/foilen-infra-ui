/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.services.MachineStatisticsService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class RotateStatsTask extends AbstractBasics {

    @Autowired
    private MachineStatisticsService machineStatisticsService;

    // Every 15 minutes
    @Scheduled(cron = "0 2,17,32,47 * * * *")
    public void sendSystemStats() {
        logger.info("Starting aggregation of statistics");
        machineStatisticsService.aggregateByHour();
        machineStatisticsService.aggregateByDay();
        logger.info("Completed aggregation of statistics");
    }

}
