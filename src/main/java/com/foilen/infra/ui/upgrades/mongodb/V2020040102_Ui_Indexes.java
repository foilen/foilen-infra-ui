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
public class V2020040102_Ui_Indexes extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {

        addIndex("auditItem", new Tuple2<>("action", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("linkType", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("resourceFirstType", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("resourceSecondType", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("tagName", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("timestamp", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("type", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("txId", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("userName", 1), new Tuple2<>("_id", -1));
        addIndex("auditItem", new Tuple2<>("userType", 1), new Tuple2<>("_id", -1));

        addIndex("machineStatistics", new Tuple2<>("machineInternalId", 1), new Tuple2<>("timestamp", 1));
        addIndex("machineStatistics", new Tuple2<>("timestamp", 1));

        addIndex("userApi", new Tuple2<>("expireOn", 1));
        addIndex("userApi", new Tuple2<>("userId", 1), new Tuple2<>("expireOn", 1));

        addIndex("userApiMachine", new Tuple2<>("expireOn", 1));
        addIndex("userApiMachine", new Tuple2<>("userId", 1), new Tuple2<>("expireOn", 1));
        addIndex("userApiMachine", new Tuple2<>("machineName", 1), new Tuple2<>("expireOn", 1));

    }

}
