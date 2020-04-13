/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.core.system.mongodb.upgrader.MongoDbUpgraderConstants;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Component
public class V2020040102_Ui_Indexes extends AbstractBasics implements UpgradeTask {

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @SafeVarargs
    private void addIndex(String collectionName, Tuple2<String, Object>... keys) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create index for collection {} , with keys {}", collectionName, keys);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        Document keysDocument = new Document();
        for (Tuple2<String, Object> key : keys) {
            keysDocument.put(key.getA(), key.getB());
        }
        collection.createIndex(keysDocument);
    }

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

    @Override
    public String useTracker() {
        return MongoDbUpgraderConstants.TRACKER;
    }

}
