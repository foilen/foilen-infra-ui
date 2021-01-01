/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.services.MachineStatisticsService;

@Component
public class RotateStatsTask {

    private final static Logger logger = LoggerFactory.getLogger(RotateStatsTask.class);

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
