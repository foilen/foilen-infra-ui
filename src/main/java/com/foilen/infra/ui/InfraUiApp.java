/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

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
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.foilen.infra.plugin.core.system.mongodb.spring.MongoDbSpringConfig;
import com.foilen.infra.ui.testonly.FoilenLoginSecurityFakeUserConfig;
import com.foilen.infra.ui.web.security.ApiSecurityConfig;
import com.foilen.login.spring.client.security.FoilenLoginSecurityConfig;
import com.foilen.login.stub.spring.client.security.FoilenLoginSecurityStubConfig;
import com.foilen.mvc.MvcConfig;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.JdbcUriTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.LogbackTools;
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

            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            } else {
                LogbackTools.changeConfig("/logback-normal.xml");
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
            case "PROD":
                // Configure login service
                File loginConfigFile = File.createTempFile("loginConfig", ".json");
                JsonTools.writeToFile(loginConfigFile, infraUiConfig.getLoginConfigDetails());
                System.setProperty("login.cookieSignatureSalt", infraUiConfig.getLoginCookieSignatureSalt());
                System.setProperty("login.configFile", loginConfigFile.getAbsolutePath());
            case "TEST":
                System.setProperty("spring.data.mongodb.uri", infraUiConfig.getMongoUri());
                String database = new JdbcUriTools("jdbc:" + infraUiConfig.getMongoUri()).getDatabase();
                if (database == null) {
                    System.err.println("Cannot get the mongodb database from the mongodb uri");
                    System.exit(1);
                }
                System.setProperty("spring.data.mongodb.database", database);
                break;
            default:
                System.out.println("Invalid mode: " + mode);
                showUsage();
                return;
            }

            List<Class<?>> sources = new ArrayList<>();

            // Run the upgrader
            logger.info("[UPGRADE] Begin");
            if (!Strings.isNullOrEmpty(infraUiConfig.getMysqlHostName())) {
                logger.info("[UPGRADE] Has MySql");
                sources.add(InfraUiUpgradesMysqlSpringConfig.class);
            }

            sources.add(MongoDbSpringConfig.class);
            sources.add(InfraUiMongoDbExtraSpringConfig.class);

            sources.add(InfraUiUpgradesMongoDbSpringConfig.class);
            sources.add(InfraUiUpgradesCommonSpringConfig.class);

            FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
            fixedBackOffPolicy.setBackOffPeriod(10000L);// 10 seconds
            RetryTemplate infiniteRetryTemplate = new RetryTemplate();
            infiniteRetryTemplate.setBackOffPolicy(fixedBackOffPolicy);

            infiniteRetryTemplate.setRetryPolicy(new AlwaysRetryPolicy());

            logger.info("[UPGRADE] Execute");
            infiniteRetryTemplate.execute(new RetryCallback<Void, RuntimeException>() {

                @Override
                public Void doWithRetry(RetryContext context) throws RuntimeException {
                    logger.info("[UPGRADE] Try upgrading");
                    try {
                        runApp(options.debug, sources, mode, false, true);
                    } catch (Throwable e) {
                        logger.info("[UPGRADE] Problem upgrading", e);
                        throw e;
                    } finally {
                        logger.info("[UPGRADE] Finished to try upgrading");
                    }
                    return null;
                }
            });

            logger.info("[UPGRADE] End");

            // Run the main app
            logger.info("[MAIN APP] Will start the main app");
            sources.clear();

            sources.add(MailConfig.class);

            sources.add(MvcConfig.class);

            // small tools
            sources.add(SpringTools.class);

            sources.add(InfraUiSpringConfig.class);

            // MongoDB
            sources.add(MongoDbSpringConfig.class);
            sources.add(InfraUiMongoDbExtraSpringConfig.class);

            // System
            sources.add(InfraUiSystemSpringConfig.class);

            // Web
            sources.add(InfraUiWebSpringConfig.class);

            // Spring Security for the external API
            sources.add(ApiSecurityConfig.class);

            // Beans per mode
            switch (mode) {
            case "TEST":
                sources.add(FoilenLoginSecurityStubConfig.class);
                sources.add(FoilenLoginSecurityFakeUserConfig.class);
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
            runApp(options.debug, sources, mode, true, false);

            // Check if debug
            if (options.debug) {
                LogbackTools.changeConfig("/logback-debug.xml");
            } else {
                LogbackTools.changeConfig("/logback-normal.xml");
            }

        } catch (Exception e) {
            logger.error("[MAIN APP] Application failed", e);
            System.exit(1);
        }

    }

    @SuppressWarnings("resource")
    private static ConfigurableApplicationContext runApp(boolean debug, List<Class<?>> sources, String mode, boolean webContext, boolean closeAtEnd) {

        System.setProperty("MODE", mode);

        ConfigurableApplicationContext ctx;
        if (webContext) {
            ConfigurableEnvironment environment = new StandardServletEnvironment();
            environment.addActiveProfile(mode);
            System.setProperty("MODE", mode);

            SpringApplication springApplication = new SpringApplication();
            springApplication.addPrimarySources(sources);
            springApplication.setEnvironment(environment);
            List<String> springBootArgs = new ArrayList<>();
            if (debug) {
                springBootArgs.add("--debug");
            }
            ConfigurableApplicationContext appCtx = springApplication.run(springBootArgs.toArray(new String[springBootArgs.size()]));
            ctx = appCtx;
        } else {
            AnnotationConfigWebApplicationContext appCtx = new AnnotationConfigWebApplicationContext();
            ctx = appCtx;
            sources.forEach(s -> appCtx.register(s));
            ctx.getEnvironment().setActiveProfiles(mode);
            ctx.refresh();
        }

        if (closeAtEnd) {
            ctx.close();
            return null;
        }
        return ctx;

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
                logger.info("[PROPERTY] Adding infraUi.{}", propertyName);
                System.setProperty("infraUi." + propertyName, propertyValue.toString());
            }
        }
    }

}
