/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb.tmp;

import com.foilen.infra.plugin.v1.core.context.TimerEventContext;
import com.foilen.infra.plugin.v1.core.eventhandler.TimerEventHandler;
import com.foilen.infra.plugin.v1.core.service.TimerService;
import com.foilen.smalltools.tools.AbstractBasics;

public class TmpTimerServiceImpl extends AbstractBasics implements TimerService {

    @Override
    public void executeLater(TimerEventHandler eventHandler) {
    }

    @Override
    public void timerAdd(TimerEventContext timer) {
    }

}
