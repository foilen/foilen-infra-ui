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
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Component
public class V2020040402_Ui_migration_delete_tmp_collections extends AbstractBasics implements UpgradeTask {

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private void dropCollection(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Drop collection {}", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();
    }

    @Override
    public void execute() {
        dropCollection("tmp_alert_to_send");
        dropCollection("tmp_api_machine_user");
        dropCollection("tmp_api_user");
        dropCollection("tmp_audit_item");
        dropCollection("tmp_machine_statistic_network");
        dropCollection("tmp_machine_statisticfs");
        dropCollection("tmp_machine_statistics");
        dropCollection("tmp_machine_statistics_fs");
        dropCollection("tmp_machine_statistics_networks");
        dropCollection("tmp_plugin_resource");
        dropCollection("tmp_plugin_resource_keep");
        dropCollection("tmp_plugin_resource_link");
        dropCollection("tmp_plugin_resource_tag");
        dropCollection("tmp_report_count");
        dropCollection("tmp_report_execution");
        dropCollection("tmp_report_time");
        dropCollection("tmp_user");
    }

    @Override
    public String useTracker() {
        return MongoDbUpgraderConstants.TRACKER;
    }

}
