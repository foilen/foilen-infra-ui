/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.tuple.Tuple2;

@Component
public class V2020041601_Ui_AuditMainIndex extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {
        addIndex("auditItem", new Tuple2<>("timestamp", -1), new Tuple2<>("_id", -1));
    }

}
