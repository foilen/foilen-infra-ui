/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.tuple.Tuple2;
import com.mongodb.client.model.IndexOptions;

@Component
public class V2021060401_Ui_CollectionsAndIndexes_Certs extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {

        addCollection("certAuthority");
        addIndex("certAuthority", new Tuple2<>("endDate", 1));
        addIndex("certAuthority", new Tuple2<>("name", 1), new Tuple2<>("startDate", 1));

        addCollection("certNode");
        addIndex("certNode", new Tuple2<>("endDate", 1));
        addIndex("certNode", new IndexOptions().unique(true), new Tuple2<>("certAuthorityName", 1), new Tuple2<>("commonName", 1));

    }

}
