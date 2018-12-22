/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.upgrader.tasks.AbstractDatabaseUpgradeTask;

@Component
public class V2018092701_add_indexes extends AbstractDatabaseUpgradeTask {

    @Override
    public void execute() {
        updateFromResource("V2018092701_add_indexes.sql");
    }

}