/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.services.UserApiService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class CleanupTask extends AbstractBasics {

    @Autowired
    private UserApiService userApiService;

    // Every hour
    @Scheduled(cron = "0 34 * * * *")
    public void cleanupApiUser() {
        logger.info("Starting deleting expired API users");
        userApiService.deleteExpired();
        logger.info("Completed deleting expired API users");
    }

}
