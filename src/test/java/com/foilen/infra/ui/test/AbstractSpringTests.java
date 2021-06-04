/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.foilen.infra.api.model.permission.OwnerRuleWithPagination;
import com.foilen.infra.plugin.core.system.mongodb.spring.MongoDbSpringConfig;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.InfraUiApp;
import com.foilen.infra.ui.InfraUiConfig;
import com.foilen.infra.ui.InfraUiMongoDbExtraSpringConfig;
import com.foilen.infra.ui.InfraUiSpringConfig;
import com.foilen.infra.ui.InfraUiSystemSpringConfig;
import com.foilen.infra.ui.localonly.FakeDataService;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.RoleRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.repositories.documents.Role;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.infra.ui.services.PaginationServiceImpl;
import com.foilen.infra.ui.upgrades.mongodb.AbstractMongoUpgradeTask;
import com.foilen.infra.ui.upgrades.mongodb.V2020041901_Ui_CollectionsAndIndexes_UserPermissions;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.login.stub.spring.LoginClientSpringStubConfig;
import com.foilen.smalltools.restapi.model.AbstractApiBaseWithError;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigUiTestConfig.class, InfraUiSpringConfig.class, InfraUiMongoDbExtraSpringConfig.class, MongoDbSpringConfig.class, InfraUiSystemSpringConfig.class,
        LoginClientSpringStubConfig.class })
@ActiveProfiles("JUNIT")
public abstract class AbstractSpringTests extends AbstractBasics {

    @Autowired
    protected AuditItemRepository auditItemRepository;
    @Autowired
    protected FakeDataService fakeDataService;
    @Autowired
    protected PaginationServiceImpl paginationServiceImpl;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected UserApiRepository userApiRepository;
    @Autowired
    protected UserHumanRepository userHumanRepository;

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
        addCollection(mongoClient, databaseName, "certAuthority");
        addCollection(mongoClient, databaseName, "certNode");
        addCollection(mongoClient, databaseName, "machineStatistics");
        addCollection(mongoClient, databaseName, "message");
        addCollection(mongoClient, databaseName, "ownerRule");
        addCollection(mongoClient, databaseName, "pluginResource");
        addCollection(mongoClient, databaseName, "pluginResourceLink");
        addCollection(mongoClient, databaseName, "reportExecution");
        addCollection(mongoClient, databaseName, "role");
        addCollection(mongoClient, databaseName, "userApi");
        addCollection(mongoClient, databaseName, "userApiMachine");
        addCollection(mongoClient, databaseName, "userHuman");

        executeTasks(new V2020041901_Ui_CollectionsAndIndexes_UserPermissions(), mongoClient, databaseName);

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

    protected Object cleanup(Page<IPResource> page) {
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> cloned = JsonTools.clone(page, TreeMap.class);
        mapsToSortedMaps(cloned);
        return cloned;
    }

    private void cleanupDocument(Object document) {
        if (document == null) {
            return;
        }
        if (document instanceof Map) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Map<String, Object> map = (Map) document;
            map.remove("createdOn");
            map.remove("createdOnText");
        } else {
            BeanWrapper beanWrapper = new BeanWrapperImpl(document);
            try {
                beanWrapper.setPropertyValue("createdOn", null);
            } catch (Exception e) {
            }
            try {
                beanWrapper.setPropertyValue("createdOnText", null);
            } catch (Exception e) {
            }
        }
    }

    protected AbstractApiBaseWithError cleanupErrors(AbstractApiBaseWithError base) {
        ApiError error = base.getError();
        if (error != null) {
            if (error.getTimestamp() != null) {
                error.setTimestamp("--hastime--");
            }
            if (error.getUniqueId() != null) {
                error.setUniqueId("--hasid--");
            }
        }
        return base;
    }

    protected OwnerRuleWithPagination cleanupIds(OwnerRuleWithPagination ownerRuleSmallWithPagination) {
        ownerRuleSmallWithPagination.getItems().forEach(it -> it.setId(null));
        return ownerRuleSmallWithPagination;
    }

    @Before
    public void createFakeData() {

        // Set no-one
        SecurityContextHolder.clearContext();

        paginationServiceImpl.setItemsPerPage(100);

        fakeDataService.clearAll();

        if (createFakeData) {
            fakeDataService.createAll();
        }

    }

    private void executeTasks(AbstractMongoUpgradeTask upgradeTask, MongoClient mongoClient, String databaseName) {
        upgradeTask.setMongoClient(mongoClient);
        upgradeTask.setDatabaseName(databaseName);
        upgradeTask.execute();
    }

    protected List<AuditItem> findAllAudits() {
        List<AuditItem> all = auditItemRepository.findAll(Sort.by("name"));
        all.forEach(a -> {
            a.setId(null);
            a.setTimestamp(null);
            cleanupDocument(a.getDocumentFrom());
            cleanupDocument(a.getDocumentTo());
        });
        return all;
    }

    protected List<Role> findAllRoles() {
        return roleRepository.findAll(Sort.by("name"));
    }

    protected List<UserApi> findAllUserApis() {
        List<UserApi> all = userApiRepository.findAll(Sort.by("userId"));
        all.forEach(it -> it.setCreatedOn(null));
        return all;
    }

    protected List<UserHuman> findAllUserHumans() {
        return userHumanRepository.findAll(Sort.by("userId"));
    }

    protected void mapsToSortedMaps(Map<String, Object> root) {
        for (String key : root.keySet()) {
            Object value = root.get(key);
            if (value instanceof Map && !(value instanceof SortedMap)) {
                @SuppressWarnings("unchecked")
                TreeMap<String, Object> cloned = JsonTools.clone(value, TreeMap.class);
                mapsToSortedMaps(cloned);
                root.put(key, cloned);
            }
        }
    }

    protected void setApiAuth(String userId) {
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(userId, null));
        SecurityContextHolder.setContext(securityContext);
    }

    protected void setFoilenAuth(String userId) {
        SecurityContext securityContext = new SecurityContextImpl();
        UserDetails userDetails = new FoilenLoginUserDetails(userId, null);
        securityContext.setAuthentication(new FoilenAuthentication(userDetails));
        SecurityContextHolder.setContext(securityContext);
    }

    protected void setResourceEditor(String editorName, IPResource... resources) {
        for (IPResource resource : resources) {
            resource.setResourceEditorName(editorName);
        }
    }

}
