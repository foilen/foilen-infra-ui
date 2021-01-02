/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mysql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.InfraUiException;
import com.foilen.smalltools.tools.TimeExecutionTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.upgrader.tasks.AbstractDatabaseUpgradeTask;
import com.google.common.base.Joiner;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Component
public class V2020040100_migrate_to_mongo extends AbstractDatabaseUpgradeTask {

    private static final long MAX_PER_BATCH = 10000;

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private void collectionNotExistsOrEmpty(String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Check collection {} not exists or emtpy", collectionName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        long count = collection.countDocuments();
        logger.info("Check collection {} count: {}", collectionName, count);
        if (count != 0) {
            throw new InfraUiException("The collection " + collectionName + " already exists and has " + count + " documents in it. Cannot migrate to this MongoDB database");
        }

    }

    private void copyTableToCollectionWithIds(String tableName) {
        copyTableToCollectionWithIds(tableName, "id");
    }

    private void copyTableToCollectionWithIds(String tableName, String... columnIds) {
        if (columnIds.length == 1) {
            copyTableToCollectionWithIdsUsingGreatherThan(tableName, columnIds[0]);
        } else {
            copyTableToCollectionWithIdsUsingOffset(tableName, columnIds);
        }

    }

    private void copyTableToCollectionWithIdsUsingGreatherThan(String tableName, String columnId) {

        String collectionName = "tmp_" + tableName;

        logger.info("Copy table to collection {} -> {}", tableName, collectionName);

        logger.info("\tDrop collection {}", collectionName);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();

        logger.info("\tCopy rows from {}", tableName);

        Tuple2<Object, Object> lastIdWrapper = new Tuple2<>();

        String sqlFirst = "SELECT * FROM " + tableName + " ORDER BY " + columnId + " LIMIT ?";
        String sqlOthers = "SELECT * FROM " + tableName + " WHERE " + columnId + " > ? ORDER BY " + columnId + " LIMIT ?";
        logger.info("\tSQL First: {}", sqlFirst);
        logger.info("\tSQL Others: {}", sqlOthers);
        AtomicBoolean completed = new AtomicBoolean();

        AtomicLong total = new AtomicLong();
        while (!completed.get()) {

            Object lastId = lastIdWrapper.getA();

            long batchExecutionMs = TimeExecutionTools.measureInMs(() -> {

                logger.info("\tBatch first id: {}", lastId);
                long mysqlGetStartMs = System.currentTimeMillis();
                List<Map<String, Object>> entities;
                if (lastId == null) {
                    entities = jdbcTemplate.queryForList(sqlFirst, MAX_PER_BATCH);
                } else {
                    entities = jdbcTemplate.queryForList(sqlOthers, lastId, MAX_PER_BATCH);
                }
                if (entities.isEmpty()) {
                    completed.set(true);
                    return;
                }
                long mysqlGetMs = System.currentTimeMillis() - mysqlGetStartMs;

                lastIdWrapper.setA(entities.get(entities.size() - 1).get(columnId));
                total.addAndGet(entities.size());

                long mongodbInsertStartMs = System.currentTimeMillis();
                collection.insertMany(entities.stream() //
                        .peek(e -> {
                            if (e.containsKey("id")) {
                                e.put("_id", e.get("id"));
                                e.remove("id");
                            }
                        }) //
                        .map(item -> new Document(item)) //
                        .collect(Collectors.toList()));
                long mongodbInsertMs = System.currentTimeMillis() - mongodbInsertStartMs;

                logger.info("\t\tMySQL Get: {} ms ; MongoDB Insert: {} ms", mysqlGetMs, mongodbInsertMs);

            });

            logger.info("\tBatch with first id: {} took {} ms", lastId, batchExecutionMs);
        }

        logger.info("\tCopied for table {} : {}", tableName, total.get());

    }

    private void copyTableToCollectionWithIdsUsingOffset(String tableName, String... columnIds) {

        String collectionName = "tmp_" + tableName;

        logger.info("Copy table to collection {} -> {}", tableName, collectionName);

        logger.info("\tDrop collection {}", collectionName);
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        collection.drop();

        logger.info("\tCopy rows from {}", tableName);

        AtomicLong offset = new AtomicLong();

        String sql = "SELECT * FROM " + tableName + " ORDER BY " + Joiner.on(",").join(columnIds) + " LIMIT ?,?";
        logger.info("\tSQL: {}", sql);
        AtomicBoolean completed = new AtomicBoolean();

        while (!completed.get()) {

            long batchExecutionMs = TimeExecutionTools.measureInMs(() -> {

                logger.info("\tBatch offset: {}", offset.get());
                long mysqlGetStartMs = System.currentTimeMillis();
                List<Map<String, Object>> entities = jdbcTemplate.queryForList(sql, offset.get(), MAX_PER_BATCH);
                if (entities.isEmpty()) {
                    completed.set(true);
                    return;
                }
                long mysqlGetMs = System.currentTimeMillis() - mysqlGetStartMs;

                offset.addAndGet(entities.size());

                long mongodbInsertStartMs = System.currentTimeMillis();
                collection.insertMany(entities.stream() //
                        .peek(e -> {
                            if (e.containsKey("id")) {
                                e.put("_id", e.get("id"));
                                e.remove("id");
                            }
                        }) //
                        .map(item -> new Document(item)) //
                        .collect(Collectors.toList()));
                long mongodbInsertMs = System.currentTimeMillis() - mongodbInsertStartMs;

                logger.info("\t\tMySQL Get: {} ms ; MongoDB Insert: {} ms", mysqlGetMs, mongodbInsertMs);

            });

            logger.info("\tBatch offset: {} took {} ms", offset, batchExecutionMs);
        }

        logger.info("\tCopied for table {} : {}", tableName, offset.get());

    }

    @Override
    public void execute() {

        // Ensure mongodb final collections are empty
        collectionNotExistsOrEmpty("auditItem");
        collectionNotExistsOrEmpty("machineStatistics");
        collectionNotExistsOrEmpty("message");
        collectionNotExistsOrEmpty("pluginResource");
        collectionNotExistsOrEmpty("pluginResourceLink");
        collectionNotExistsOrEmpty("reportExecution");
        collectionNotExistsOrEmpty("userApi");
        collectionNotExistsOrEmpty("userApiMachine");
        collectionNotExistsOrEmpty("userHuman");

        // Copy tables to documents as-is in a temporary collection
        copyTableToCollectionWithIds("alert_to_send");
        copyTableToCollectionWithIds("api_machine_user");
        copyTableToCollectionWithIds("api_user");
        copyTableToCollectionWithIds("audit_item");
        copyTableToCollectionWithIds("machine_statistic_network");
        copyTableToCollectionWithIds("machine_statisticfs");
        copyTableToCollectionWithIds("machine_statistics");
        copyTableToCollectionWithIds("machine_statistics_fs", "machine_statistics_id", "fs_id");
        copyTableToCollectionWithIds("machine_statistics_networks", "machine_statistics_id", "networks_id");
        copyTableToCollectionWithIds("plugin_resource");
        copyTableToCollectionWithIds("plugin_resource_link");
        copyTableToCollectionWithIds("plugin_resource_tag");
        copyTableToCollectionWithIds("report_count");
        copyTableToCollectionWithIds("report_execution");
        copyTableToCollectionWithIds("report_time");
        copyTableToCollectionWithIds("user");

    }

    @Override
    public String useTracker() {
        return "db";
    }

}
