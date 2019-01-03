/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.foilen.infra.ui.web.security.ApiSecurityConfig;
import com.foilen.login.spring.client.security.FoilenLoginSecurityConfig;
import com.foilen.login.stub.spring.client.security.FoilenLoginSecurityStubConfig;
import com.foilen.mvc.MvcConfig;
import com.foilen.services.AlertNotificationServiceImpl;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.LogbackTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.SpringTools;
import com.foilen.springconfig.MailConfig;
import com.google.common.base.Strings;

public class InfraUiApp {

    static private final Logger logger = LoggerFactory.getLogger(InfraUiApp.class);

    public static void main(String[] args) {

        try {
            // Get the parameters
            InfraUiOptions options = new InfraUiOptions();
            CmdLineParser cmdLineParser = new CmdLineParser(options);
            try {
                cmdLineParser.parseArgument(args);
            } catch (CmdLineException e) {
                e.printStackTrace();
                showUsage();
                return;
            }

            List<String> springBootArgs = new ArrayList<>();
            if (options.debug) {
                springBootArgs.add("--debug");
            }

            // Set the environment
            String mode = options.mode;
            ConfigurableEnvironment environment = new StandardServletEnvironment();
            environment.addActiveProfile(mode);

            // Get the configuration from options or environment
            String configFile = options.configFile;
            if (Strings.isNullOrEmpty(configFile)) {
                configFile = environment.getProperty("CONFIG_FILE");
            }
            InfraUiConfig infraUiConfig;
            if (Strings.isNullOrEmpty(configFile)) {
                infraUiConfig = new InfraUiConfig();
            } else {
                infraUiConfig = JsonTools.readFromFile(configFile, InfraUiConfig.class);
            }

            // Local -> Add some mock values
            if ("LOCAL".equals(mode)) {
                logger.info("Setting some random values for LOCAL mode");

                infraUiConfig.setBaseUrl("http://127.0.0.1:8080");

                infraUiConfig.setMysqlDatabaseUserName("notNeeded");
                infraUiConfig.setMysqlDatabasePassword("notNeeded");

                infraUiConfig.setMailFrom("infra@example.com");
                infraUiConfig.setMailAlertsTo("alerts@example.com");

                infraUiConfig.getLoginConfigDetails().setBaseUrl("http://login.example.com");

                infraUiConfig.setCsrfSalt(SecureRandomTools.randomBase64String(10));
                infraUiConfig.setLoginCookieSignatureSalt(SecureRandomTools.randomBase64String(10));

            }

            // Override some database configuration if provided via environment
            String overrideMysqlHostName = System.getenv("MYSQL_PORT_3306_TCP_ADDR");
            if (!Strings.isNullOrEmpty(overrideMysqlHostName)) {
                infraUiConfig.setMysqlHostName(overrideMysqlHostName);
            }
            String overrideMysqlPort = System.getenv("MYSQL_PORT_3306_TCP_PORT");
            if (!Strings.isNullOrEmpty(overrideMysqlPort)) {
                infraUiConfig.setMysqlPort(Integer.valueOf(overrideMysqlPort));
            }

            // Override misc configuration if provided via environment
            String overrideInfiniteLoopTimeoutInMs = System.getenv("INFINITE_LOOP_TIMEOUT_IN_MS");
            if (!Strings.isNullOrEmpty(overrideInfiniteLoopTimeoutInMs)) {
                infraUiConfig.setInfiniteLoopTimeoutInMs(Long.valueOf(overrideInfiniteLoopTimeoutInMs));
            }

            // Check needed config and add it to the known properties
            uiConfigToSystemProperties(infraUiConfig);

            // Config per mode
            switch (mode) {
            case "LOCAL":
                break;
            case "PROD":
                // Configure login service
                File loginConfigFile = File.createTempFile("loginConfig", ".json");
                JsonTools.writeToFile(loginConfigFile, infraUiConfig.getLoginConfigDetails());
                System.setProperty("login.cookieSignatureSalt", infraUiConfig.getLoginCookieSignatureSalt());
                System.setProperty("login.configFile", loginConfigFile.getAbsolutePath());
            case "TEST":
                // Configure database
                System.setProperty("spring.datasource.url", "jdbc:mysql://" + infraUiConfig.getMysqlHostName() + ":" + infraUiConfig.getMysqlPort() + "/" + infraUiConfig.getMysqlDatabaseName());
                System.setProperty("spring.datasource.username", infraUiConfig.getMysqlDatabaseUserName());
                System.setProperty("spring.datasource.password", infraUiConfig.getMysqlDatabasePassword());
                break;
            default:
                System.out.println("Invalid mode: " + mode);
                showUsage();
                return;
            }

            List<Class<?>> sources = new ArrayList<>();

            // Run the upgrader
            if ("LOCAL".equals(mode)) {
                logger.info("Skipping UPGRADE MODE");
            } else {
                logger.info("Begin UPGRADE MODE");
                sources.add(InfraUiUpgradesSpringConfig.class);

                RetryTemplate infiniteRetryTemplate = new RetryTemplate();

                FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
                fixedBackOffPolicy.setBackOffPeriod(10000L);// 10 seconds
                infiniteRetryTemplate.setBackOffPolicy(fixedBackOffPolicy);

                infiniteRetryTemplate.setRetryPolicy(new AlwaysRetryPolicy());

                infiniteRetryTemplate.execute(new RetryCallback<Void, RuntimeException>() {

                    @Override
                    public Void doWithRetry(RetryContext context) throws RuntimeException {
                        runApp(springBootArgs, sources, mode, true);
                        return null;
                    }
                });

                logger.info("End UPGRADE MODE");
            }

            // Run the main app
            logger.info("Will start the main app");
            sources.clear();

            sources.add(AlertNotificationServiceImpl.class);
            sources.add(MailConfig.class);

            sources.add(MvcConfig.class);

            // small tools
            sources.add(SpringTools.class);

            sources.add(InfraUiSpringConfig.class);
            sources.add(InfraUiDbLiveSpringConfig.class);
            sources.add(InfraUiWebSpringConfig.class);

            // Spring Security for the external API
            sources.add(ApiSecurityConfig.class);

            // Beans per mode
            switch (mode) {
            case "LOCAL":
            case "TEST":
                sources.add(FoilenLoginSecurityStubConfig.class);
                break;
            case "PROD":
                // foilen-login-api
                sources.add(FoilenLoginSecurityConfig.class);
                break;
            default:
                System.out.println("Invalid mode: " + mode);
                showUsage();
                return;
            }

            // Start
            runApp(springBootArgs, sources, mode, false);

            // Check if debug
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            }

        } catch (Exception e) {
            logger.error("Application failed", e);
            System.exit(1);
        }

    }

    private static ConfigurableApplicationContext runApp(List<String> springBootArgs, List<Class<?>> sources, String mode, boolean closeAtEnd) {

        // Set the environment
        ConfigurableEnvironment environment = new StandardServletEnvironment();
        environment.addActiveProfile(mode);
        System.setProperty("MODE", mode);

        SpringApplication springApplication = new SpringApplication(sources.toArray());
        springApplication.setEnvironment(environment);
        ConfigurableApplicationContext appCtx = springApplication.run(springBootArgs.toArray(new String[springBootArgs.size()]));
        if (closeAtEnd) {
            appCtx.close();
        }
        return appCtx;
    }

    private static void showUsage() {
        System.out.println("Usage:");
        CmdLineParser cmdLineParser = new CmdLineParser(new InfraUiOptions());
        cmdLineParser.printUsage(System.out);
    }

    public static void uiConfigToSystemProperties(InfraUiConfig infraUiConfig) {
        BeanWrapper infraUiConfigBeanWrapper = new BeanWrapperImpl(infraUiConfig);
        for (PropertyDescriptor propertyDescriptor : infraUiConfigBeanWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            Object propertyValue = infraUiConfigBeanWrapper.getPropertyValue(propertyName);
            if (propertyValue == null || propertyValue.toString().isEmpty()) {
                if (ReflectionTools.findAnnotationByFieldNameAndAnnotation(InfraUiConfig.class, propertyName, Nullable.class) == null) {
                    System.err.println(propertyName + " in the config cannot be null or empty");
                    System.exit(1);
                }
            } else {
                System.setProperty("infraUi." + propertyName, propertyValue.toString());
            }
        }
    }

}
