/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

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

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.InfraUiApp;
import com.foilen.infra.ui.InfraUiConfig;
import com.foilen.infra.ui.InfraUiSpringConfig;
import com.foilen.infra.ui.localonly.FakeDataService;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.smalltools.tools.SecureRandomTools;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigUiTestConfig.class, InfraUiSpringConfig.class, AlertNotificationServiceStub.class })
@ActiveProfiles("JUNIT")
public abstract class AbstractSpringTests {

    @Autowired
    protected FakeDataService fakeDataService;

    private boolean createFakeData;

    public AbstractSpringTests(boolean createFakeData) {
        InfraUiConfig infraUiConfig = new InfraUiConfig();
        infraUiConfig.setBaseUrl("https://infra.example.com");
        infraUiConfig.setMysqlDatabaseUserName("_MYSQL_USER_NAME_");
        infraUiConfig.setMysqlDatabasePassword("_MYSQL_PASSWORD_");
        infraUiConfig.setMailFrom("infra@example.com");
        infraUiConfig.setMailAlertsTo("alerts@example.com");
        infraUiConfig.getLoginConfigDetails().setBaseUrl("http://login.example.com");
        infraUiConfig.setCsrfSalt(SecureRandomTools.randomBase64String(10));
        infraUiConfig.setLoginCookieSignatureSalt(SecureRandomTools.randomBase64String(10));
        InfraUiApp.uiConfigToSystemProperties(infraUiConfig);

        System.setProperty("MODE", "JUNIT");
        this.createFakeData = createFakeData;
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
