/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.foilen.infra.plugin.core.system.junits.AbstractIPResourceServiceTest;
import com.foilen.infra.plugin.core.system.mongodb.spring.MongoDbSpringConfig;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.infra.ui.InfraUiMongoDbExtraSpringConfig;
import com.foilen.infra.ui.InfraUiSpringConfig;
import com.foilen.infra.ui.InfraUiSystemSpringConfig;
import com.foilen.infra.ui.test.ConfigUiTestConfig;
import com.foilen.infra.ui.test.mock.FakeDataService;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigUiTestConfig.class, InfraUiSpringConfig.class, InfraUiMongoDbExtraSpringConfig.class, MongoDbSpringConfig.class, InfraUiSystemSpringConfig.class })
@ActiveProfiles("JUNIT")
public class PluginSystemServicesImplTest extends AbstractIPResourceServiceTest {

    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private FakeDataService fakeDataService;
    @Autowired
    private IPResourceService ipResourceService;

    public PluginSystemServicesImplTest() {
        init();
    }

    private void addCollection(MongoClient mongoClient, String databaseName, String collectionName) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);

        logger.info("Create collection {}", collectionName);
        try {
            mongoDatabase.createCollection(collectionName);
        } catch (MongoCommandException e) {
            if (e.getErrorCode() != 48) { // Already exists
                throw e;
            }
        }
    }

    @Override
    @Before
    public void beforeEach() {

        init();

        fakeDataService.clearAll();

        UnixUserAvailableIdHelper.init(ipResourceService);
        super.beforeEach();
    }

    @Override
    protected CommonServicesContext getCommonServicesContext() {
        return commonServicesContext;
    }

    @Override
    protected InternalServicesContext getInternalServicesContext() {
        return internalServicesContext;
    }

    private void init() {
        String mongoUri = "mongodb://127.0.0.1:27085/";
        String databaseName = "junits";
        System.setProperty("MODE", "JUNIT");
        System.setProperty("spring.data.mongodb.uri", mongoUri);
        System.setProperty("spring.data.mongodb.database", databaseName);

        // Create collections
        MongoClient mongoClient = MongoClients.create(mongoUri);
        addCollection(mongoClient, databaseName, "auditItem");
        addCollection(mongoClient, databaseName, "machineStatistics");
        addCollection(mongoClient, databaseName, "message");
        addCollection(mongoClient, databaseName, "pluginResource");
        addCollection(mongoClient, databaseName, "pluginResourceLink");
        addCollection(mongoClient, databaseName, "reportExecution");
        addCollection(mongoClient, databaseName, "userApi");
        addCollection(mongoClient, databaseName, "userApiMachine");
        addCollection(mongoClient, databaseName, "userHuman");
        mongoClient.close();

        System.setProperty("MODE", "JUNIT");
        System.setProperty("infraUi.csrfSalt", "aaaaaa");
        System.setProperty("infraUi.baseUrl", "https://infra.example.com");
        System.setProperty("infraUi.mailFrom", "nope_example.com");
        System.setProperty("infraUi.mailAlertsTo", "nope_example.com");
        System.setProperty("infraUi.infiniteLoopTimeoutInMs", "20000");
    }

    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @Test(timeout = 30000)
    @Override
    public void testInfiniteLoop() {
        super.testInfiniteLoop();
    }

}
