/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.springframework.stereotype.Component;

@Component
public class V2020040101_Ui_Collections extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {
        addCollection("auditItem");
        addCollection("machineStatistics");
        addCollection("reportExecution");
        addCollection("userApi");
        addCollection("userApiMachine");
        addCollection("userHuman");
    }

}
