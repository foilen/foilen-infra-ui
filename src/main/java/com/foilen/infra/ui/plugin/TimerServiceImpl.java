/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.plugin;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.smalltools.tools.AbstractBasics;

// TODO Timer - Use Quartz
@Component
public class TimerServiceImpl extends AbstractBasics implements TimerService {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;

    private ExecutorService waitingExecutorService = Executors.newCachedThreadPool();
    private ExecutorService executingExecutorService = Executors.newCachedThreadPool();

    @Override
    public void executeLater(TimerEventHandler eventHandler) {
        timerAdd(new TimerEventContext(eventHandler, "executeLater", Calendar.MILLISECOND, 500, true, true));
    }

    @Override
    public void timerAdd(TimerEventContext timer) {
        waitingExecutorService.submit(new TimerWaitingRunnable(commonServicesContext, internalServicesContext, executingExecutorService, timer));
    }

}
