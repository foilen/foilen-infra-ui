/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;

public class TimerExecutionRunnable extends AbstractBasics implements Runnable {

    private CommonServicesContext commonServicesContext;
    private InternalServicesContext internalServicesContext;

    private TimerEventContext timer;

    public TimerExecutionRunnable(CommonServicesContext commonServicesContext, InternalServicesContext internalServicesContext, TimerEventContext timer) {
        this.commonServicesContext = commonServicesContext;
        this.internalServicesContext = internalServicesContext;
        this.timer = timer;
    }

    @Override
    public void run() {

        // Change the thread name
        ThreadNameStateTool nameThread = ThreadTools.nameThread().setSeparator("/").clear() //
                .appendText("Timer").appendText(timer.getTimerName()) //
                .change();

        try {
            ChangesContext changes = new ChangesContext(commonServicesContext.getResourceService());
            timer.getTimerEventHandler().timerHandler(commonServicesContext, changes, timer);
            internalServicesContext.getInternalChangeService().changesExecute(changes);
        } catch (Throwable e) {
            logger.error("Problem executing the timer", e);
        } finally {
            nameThread.revert();
        }
    }
}