/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mysql;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.upgrader.tasks.AbstractDatabaseUpgradeTask;

@Component
public class V2017121401_Initial extends AbstractDatabaseUpgradeTask {

    @Override
    public void execute() {
        updateFromResource("V2017121401_Initial.sql");
    }

    @Override
    public String useTracker() {
        return "db";
    }

}