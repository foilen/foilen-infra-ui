/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.core.system.common.service.IPPluginServiceImpl;
import com.foilen.infra.plugin.core.system.mongodb.repositories.MessageRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceLinkRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.Message;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResourceLink;
import com.foilen.infra.plugin.core.system.mongodb.service.ResourceDefinitionService;
import com.foilen.infra.plugin.core.system.mongodb.service.ResourceDefinitionServiceImpl;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.MachineStatisticsRepository;
import com.foilen.infra.ui.repositories.ReportExecutionRepository;
import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.repositories.documents.MachineStatistics;
import com.foilen.infra.ui.repositories.documents.ReportExecution;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.infra.ui.repositories.documents.models.MachineStatisticFS;
import com.foilen.infra.ui.repositories.documents.models.MachineStatisticNetwork;
import com.foilen.infra.ui.repositories.documents.models.ReportCount;
import com.foilen.infra.ui.repositories.documents.models.ReportTime;
import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpIPResourceServiceImpl;
import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpInternalIPResourceServiceImpl;
import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpTimerServiceImpl;
import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpTranslationServiceImpl;
import com.foilen.smalltools.tools.BufferBatchesTools;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.util.concurrent.RateLimiter;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;

@Component
public class V2020040401_Ui_migration_tmp_to_new extends AbstractMongoUpgradeTask {

    @Autowired
    private AuditItemRepository auditItemRepository;
    @Autowired
    private MachineStatisticsRepository machineStatisticsRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private PluginResourceLinkRepository pluginResourceLinkRepository;
    @Autowired
    private ReportExecutionRepository reportExecutionRepository;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;
    @Autowired
    private UserApiRepository userApiRepository;
    @Autowired
    private UserHumanRepository userHumanRepository;

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {

        RateLimiter rateLimiter = RateLimiter.create(1);

        addIndex("tmp_machine_statistics_fs", new Tuple2<>("machine_statistics_id", 1));
        addIndex("tmp_machine_statistics_networks", new Tuple2<>("machine_statistics_id", 1));
        addIndex("tmp_plugin_resource_tag", new Tuple2<>("plugin_resource_id", 1));
        addIndex("tmp_report_count", new Tuple2<>("report_execution_id", 1));
        addIndex("tmp_report_time", new Tuple2<>("report_execution_id", 1));

        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("tmp_alert_to_send -> message");
        messageRepository.deleteAll();
        BufferBatchesTools.<Message> autoClose(10000, entities -> {
            messageRepository.saveAll(entities);
        }, bbt -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_alert_to_send");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.project(new Document() //
                            .append("level", "INFO") //
                            .append("sentOn", "$sent_on") //
                            .append("sender", 1) //
                            .append("shortDescription", "$subject") //
                            .append("longDescription", "$content") //
                            .append("acknowledged", "false") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                Message entity = new Message();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_api_machine_user -> userApiMachine");
        userApiMachineRepository.deleteAll();
        BufferBatchesTools.<UserApiMachine> autoClose(10000, entities -> {
            userApiMachineRepository.saveAll(entities);
        }, bbt -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_api_machine_user");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_api_user", "_id", "_id", "fromItems"), //
                    Aggregates.replaceRoot(Document.parse("{ $mergeObjects: [ { $arrayElemAt: [ \"$fromItems\", 0 ] }, \"$$ROOT\" ] }")), //
                    Aggregates.project(new Document() //
                            .append("machineName", "$machine_name") //
                            .append("userKey", "$user_key") //
                            .append("userId", "$user_id") //
                            .append("userHashedKey", "$user_hashed_key") //
                            .append("description", 1) //
                            .append("admin", "$is_admin") //
                            .append("createdOn", "$created_on") //
                            .append("expireOn", "$expire_on") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                UserApiMachine entity = new UserApiMachine();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_api_user -> userApi");
        userApiRepository.deleteAll();
        BufferBatchesTools.<UserApi> autoClose(10000, entities -> {
            userApiRepository.saveAll(entities);
        }, bbt -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_api_user");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_api_machine_user", "_id", "_id", "fromItems"), //
                    Aggregates.match(Filters.size("fromItems", 0)), //
                    Aggregates.project(new Document() //
                            .append("userId", "$user_id") //
                            .append("userHashedKey", "$user_hashed_key") //
                            .append("description", 1) //
                            .append("admin", "$is_admin") //
                            .append("createdOn", "$created_on") //
                            .append("expireOn", "$expire_on") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                UserApi entity = new UserApi();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_plugin_resource, tmp_plugin_resource_tag -> pluginResource");
        pluginResourceRepository.deleteAll();
        MongoCollection<Document> keepCollection = mongoDatabase.getCollection("tmp_plugin_resource_keep");
        keepCollection.drop();
        BufferBatchesTools.<Tuple2<Object, PluginResource>> autoClose(10000, entities -> {
            List<PluginResource> pluginResources = pluginResourceRepository.saveAll(entities.stream().map(it -> it.getB()).collect(Collectors.toList()));
            for (int i = 0; i < pluginResources.size(); ++i) {
                PluginResource pluginResource = pluginResources.get(i);
                keepCollection.insertOne(new Document("_id", entities.get(i).getA()) //
                        .append("type", pluginResource.getType()) //
                        .append("newId", pluginResource.getId()) //
                );
            }
        }, bbt -> {

            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_plugin_resource");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();

            // Load all plugins definition
            System.setProperty("PluginUpgrader.disable", "true");
            IPPluginServiceImpl ipPluginServiceImpl = new IPPluginServiceImpl();
            ResourceDefinitionService resourceDefinitionService = new ResourceDefinitionServiceImpl();
            CommonServicesContext commonServicesContext = new CommonServicesContext(null, ipPluginServiceImpl, new TmpIPResourceServiceImpl(resourceDefinitionService), new TmpTimerServiceImpl(),
                    new TmpTranslationServiceImpl());
            InternalServicesContext internalServicesContext = new InternalServicesContext(new TmpInternalIPResourceServiceImpl(resourceDefinitionService), null);
            ipPluginServiceImpl.loadPlugins(commonServicesContext, internalServicesContext);
            System.setProperty("PluginUpgrader.disable", "false");

            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_plugin_resource_tag", "_id", "plugin_resource_id", "tags"), //
                    Aggregates.project(new Document() //
                            .append("editorName", "$editor_name") //
                            .append("type", 1) //
                            .append("resource", "$value_json") //
                            .append("tags", 1) //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                Class<? extends IPResource> resourceClass = resourceDefinitionService.getResourceDefinition(d.getString("type")).getResourceClass();
                IPResource resource = JsonTools.readFromString(d.getString("resource"), resourceClass);
                Map<String, Object> fullDocument = JsonTools.clone(d, Map.class);

                Object id = fullDocument.remove("_id");

                fullDocument.put("resource", resource);

                fullDocument.put("tags", ((List<Map<String, Object>>) fullDocument.get("tags")).stream() //
                        .map(m -> (String) m.get("tag_name")) //
                        .collect(Collectors.toList()) //
                );

                PluginResource entity = new PluginResource();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(fullDocument);
                bbt.add(new Tuple2<>(id, entity));

            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_plugin_resource_link -> pluginResourceLink");
        pluginResourceLinkRepository.deleteAll();
        BufferBatchesTools.<PluginResourceLink> autoClose(10000, entities -> {
            pluginResourceLinkRepository.saveAll(entities);
        }, bbt -> {

            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_plugin_resource_link");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();

            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_plugin_resource_keep", "from_plugin_resource_id", "_id", "fromResource"), //
                    Aggregates.lookup("tmp_plugin_resource_keep", "to_plugin_resource_id", "_id", "toResource"), //
                    Aggregates.project(new Document() //
                            .append("fromResourceId", "$fromResource.newId") //
                            .append("fromResourceType", "$fromResource.type") //
                            .append("linkType", "$link_type") //
                            .append("toResourceId", "$toResource.newId") //
                            .append("toResourceType", "$toResource.type") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                d.put("fromResourceId", d.getList("fromResourceId", String.class).get(0));
                d.put("fromResourceType", d.getList("fromResourceType", String.class).get(0));
                d.put("toResourceId", d.getList("toResourceId", String.class).get(0));
                d.put("toResourceType", d.getList("toResourceType", String.class).get(0));

                PluginResourceLink entity = new PluginResourceLink();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_user -> userHuman");
        userHumanRepository.deleteAll();
        BufferBatchesTools.<UserHuman> autoClose(10000, entities -> {
            userHumanRepository.saveAll(entities);
        }, bbt -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_user");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.project(new Document() //
                            .append("userId", "$user_id") //
                            .append("admin", "$is_admin") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                UserHuman entity = new UserHuman();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_machine_statistics, tmp_machine_statisticfs, tmp_machine_statistics_fs, tmp_machine_statistics_networks, tmp_machine_statistic_network -> machineStatistics");
        machineStatisticsRepository.deleteAll();
        BufferBatchesTools.<MachineStatistics> autoClose(10000, entities -> {
            machineStatisticsRepository.saveAll(entities);
        }, bbt -> {

            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_machine_statistics");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_machine_statistics_fs", "_id", "machine_statistics_id", "tmp_fs"), //
                    Aggregates.lookup("tmp_machine_statisticfs", "tmp_fs.fs_id", "_id", "fs"), //
                    Aggregates.lookup("tmp_machine_statistics_networks", "_id", "machine_statistics_id", "tmp_networks"), //
                    Aggregates.lookup("tmp_machine_statistic_network", "tmp_networks.networks_id", "_id", "networks"), //
                    Aggregates.lookup("tmp_plugin_resource_keep", "machine_internal_id", "_id", "machineInternalId"), //
                    Aggregates.project(new Document() //
                            .append("machineInternalId", "$machineInternalId.newId") //
                            .append("timestamp", 1) //
                            .append("cpuUsed", "$cpu_used") //
                            .append("cpuTotal", "$cpu_total") //
                            .append("memoryUsed", "$memory_used") //
                            .append("memoryTotal", "$memory_total") //
                            .append("memorySwapUsed", "$memory_swap_used") //
                            .append("memorySwapTotal", "$memory_swap_total") //
                            .append("aggregationsForDay", "$aggregations_for_day") //
                            .append("aggregationsForHour", "$aggregations_for_hour") //
                            .append("fs", 1) //
                            .append("networks", 1) //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                List<String> machineInternalIds = d.getList("machineInternalId", String.class);
                if (CollectionsTools.isNullOrEmpty(machineInternalIds)) {
                    d.remove("machineInternalId");
                } else {
                    d.put("machineInternalId", machineInternalIds.get(0));
                }
                Map<String, Object> fullDocument = JsonTools.clone(d, Map.class);

                fullDocument.remove("_id");
                fullDocument.put("timestamp", new Date((long) fullDocument.get("timestamp")));

                fullDocument.put("fs", ((List<Map<String, Object>>) fullDocument.get("fs")).stream() //
                        .map(m -> {

                            m.remove("_id");
                            rename(m, "is_root", "root");
                            rename(m, "used_space", "usedSpace");
                            rename(m, "total_space", "totalSpace");

                            MachineStatisticFS entity = new MachineStatisticFS();
                            BeanWrapper wrapper = new BeanWrapperImpl(entity);
                            wrapper.setPropertyValues(m);
                            return entity;
                        }).collect(Collectors.toList()) //
                );
                fullDocument.put("networks", ((List<Map<String, Object>>) fullDocument.get("networks")).stream() //
                        .map(m -> {

                            m.remove("_id");
                            rename(m, "interface_name", "interfaceName");
                            rename(m, "in_bytes", "inBytes");
                            rename(m, "out_bytes", "outBytes");

                            MachineStatisticNetwork entity = new MachineStatisticNetwork();
                            BeanWrapper wrapper = new BeanWrapperImpl(entity);
                            wrapper.setPropertyValues(m);
                            return entity;
                        }).collect(Collectors.toList()) //
                );

                MachineStatistics entity = new MachineStatistics();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(fullDocument);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_report_execution, tmp_report_count, tmp_report_time -> reportExecution");
        reportExecutionRepository.deleteAll();
        BufferBatchesTools.<ReportExecution> autoClose(10000, entities -> {
            reportExecutionRepository.saveAll(entities);
        }, bbt -> {

            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_report_execution");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.lookup("tmp_report_count", "_id", "report_execution_id", "reportCounts"), //
                    Aggregates.lookup("tmp_report_time", "_id", "report_execution_id", "reportTimes"), //
                    Aggregates.project(new Document() //
                            .append("txId", "$tx_id") //
                            .append("timestamp", 1) //
                            .append("success", 1) //
                            .append("reportCounts", 1) //
                            .append("reportTimes", 1) //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                Map<String, Object> fullDocument = JsonTools.clone(d, Map.class);

                fullDocument.remove("_id");
                fullDocument.put("timestamp", new Date((long) fullDocument.get("timestamp")));

                fullDocument.put("reportCounts", ((List<Map<String, Object>>) fullDocument.get("reportCounts")).stream() //
                        .map(m -> {

                            m.remove("_id");
                            m.remove("report_execution_id");
                            rename(m, "resource_simple_class_name_and_resource_name", "resourceSimpleClassNameAndResourceName");

                            ReportCount entity = new ReportCount();
                            BeanWrapper wrapper = new BeanWrapperImpl(entity);
                            wrapper.setPropertyValues(m);
                            return entity;
                        }).collect(Collectors.toList()) //
                );

                fullDocument.put("reportTimes", ((List<Map<String, Object>>) fullDocument.get("reportTimes")).stream() //
                        .map(m -> {

                            m.remove("_id");
                            m.remove("report_execution_id");
                            rename(m, "time_in_ms", "timeInMs");
                            rename(m, "update_event_handler_simple_class_name", "updateEventHandlerSimpleClassName");

                            ReportTime entity = new ReportTime();
                            BeanWrapper wrapper = new BeanWrapperImpl(entity);
                            wrapper.setPropertyValues(m);
                            return entity;
                        }).collect(Collectors.toList()) //
                );

                ReportExecution entity = new ReportExecution();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(fullDocument);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

        logger.info("tmp_audit_item -> auditItem");
        auditItemRepository.deleteAll();
        BufferBatchesTools.<AuditItem> autoClose(10000, entities -> {
            auditItemRepository.saveAll(entities);
        }, bbt -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection("tmp_audit_item");

            long total = collection.countDocuments();
            AtomicLong done = new AtomicLong();
            collection.aggregate(Arrays.asList( //
                    Aggregates.project(new Document() //
                            .append("timestamp", 1) //
                            .append("txId", "$tx_id") //
                            .append("explicitChange", "$explicit_change") //
                            .append("type", 1) //
                            .append("action", 1) //
                            .append("userType", "$user_type") //
                            .append("userName", "$user_name") //
                            .append("resourceFirstType", "$resource_first_type") //
                            .append("resourceFirst", "$resource_first") //
                            .append("resourceSecondType", "$resource_second_type") //
                            .append("resourceSecond", "$resource_second") //
                            .append("linkType", "$link_type") //
                            .append("tagName", "$tag_name") //
            ) //
            )).forEach((Consumer<Document>) d -> {

                long doneNow = done.incrementAndGet();
                if (rateLimiter.tryAcquire()) {
                    logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));
                }

                d.remove("_id");

                jsonToObject(d, "resourceFirst");
                jsonToObject(d, "resourceSecond");

                AuditItem entity = new AuditItem();
                BeanWrapper wrapper = new BeanWrapperImpl(entity);
                wrapper.setPropertyValues(d);
                bbt.add(entity);
            });

            long doneNow = done.get();
            logger.info("\t{} / {} ({} %)", doneNow, total, percent(total, doneNow));

        });

    }

    private void jsonToObject(Document d, String fieldName) {
        String json = d.getString(fieldName);
        if (json == null) {
            return;
        }
        d.put(fieldName, JsonTools.readFromString(json, Object.class));
    }

    private long percent(long total, long doneNow) {
        if (total == 0) {
            return 100;
        }
        return doneNow * 100 / total;
    }

    private void rename(Map<String, Object> m, String oldName, String newName) {
        m.put(newName, m.remove(oldName));
    }

}
