/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.upgrader.tasks.AbstractDatabaseUpgradeTask;

@Component
public class V2019040301_add_alert_to_send extends AbstractDatabaseUpgradeTask {

    @Override
    public void execute() {
        updateFromResource("V2019040301_add_alert_to_send.sql");
    }

}
