/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.foilen.infra.plugin.core.system.mongodb.spring.MongoDbSpringConfig;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.InfraUiApp;
import com.foilen.infra.ui.InfraUiConfig;
import com.foilen.infra.ui.InfraUiMongoDbExtraSpringConfig;
import com.foilen.infra.ui.InfraUiSpringConfig;
import com.foilen.infra.ui.InfraUiSystemSpringConfig;
import com.foilen.infra.ui.test.mock.FakeDataService;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigUiTestConfig.class, InfraUiSpringConfig.class, InfraUiMongoDbExtraSpringConfig.class, MongoDbSpringConfig.class, InfraUiSystemSpringConfig.class })
@ActiveProfiles("JUNIT")
public abstract class AbstractSpringTests extends AbstractBasics {

    @Autowired
    protected FakeDataService fakeDataService;

    private boolean createFakeData;

    public AbstractSpringTests(boolean createFakeData) {

        String mongoUri = "mongodb://127.0.0.1:27085/";

        InfraUiConfig infraUiConfig = new InfraUiConfig();
        infraUiConfig.setBaseUrl("https://infra.example.com");
        infraUiConfig.setMailFrom("infra@example.com");
        infraUiConfig.setMailAlertsTo("alerts@example.com");
        infraUiConfig.setMongoUri(mongoUri);
        infraUiConfig.getLoginConfigDetails().setBaseUrl("http://login.example.com");
        infraUiConfig.setCsrfSalt(SecureRandomTools.randomBase64String(10));
        infraUiConfig.setLoginCookieSignatureSalt(SecureRandomTools.randomBase64String(10));
        InfraUiApp.uiConfigToSystemProperties(infraUiConfig);

        String databaseName = "junits";
        System.setProperty("MODE", "JUNIT");
        System.setProperty("spring.data.mongodb.uri", mongoUri);
        System.setProperty("spring.data.mongodb.database", databaseName);
        this.createFakeData = createFakeData;

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

    @Before
    public void createFakeData() {

        // Set no-one
        SecurityContextHolder.clearContext();

        fakeDataService.clearAll();

        if (createFakeData) {
            fakeDataService.createAll();
        }
    }

    protected void setFoilenAuth(String userId, String email) {
        SecurityContext securityContext = new SecurityContextImpl();
        UserDetails userDetails = new FoilenLoginUserDetails(userId, email);
        securityContext.setAuthentication(new FoilenAuthentication(userDetails));
        SecurityContextHolder.setContext(securityContext);
    }

    protected void setResourceEditor(String editorName, IPResource... resources) {
        for (IPResource resource : resources) {
            resource.setResourceEditorName(editorName);
        }
    }

}
