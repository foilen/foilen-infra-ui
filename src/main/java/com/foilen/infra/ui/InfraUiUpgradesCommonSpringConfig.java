/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;

import com.foilen.infra.plugin.core.system.mongodb.upgrader.MongoDbUpgraderConstants;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.ui.services.PaginationService;
import com.foilen.infra.ui.services.PaginationServiceImpl;
import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpTranslationServiceImpl;
import com.foilen.smalltools.upgrader.UpgraderTools;
import com.foilen.smalltools.upgrader.tasks.UpgradeTask;
import com.foilen.smalltools.upgrader.trackers.DatabaseUpgraderTracker;
import com.foilen.smalltools.upgrader.trackers.MongoDbUpgraderTracker;
import com.mongodb.client.MongoClient;

@Configuration
public class InfraUiUpgradesCommonSpringConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer PropertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoClient mongoClient;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    public ConversionService conversionService(List<Converter<?, ?>> converters) {
        DefaultConversionService conversionService = new DefaultConversionService();
        converters.forEach(c -> conversionService.addConverter(c));
        return conversionService;
    }

    @Bean
    public PaginationService paginationService() {
        return new PaginationServiceImpl();
    }

    @Bean
    public TranslationService translationService() {
        return new TmpTranslationServiceImpl();
    }

    @Bean
    public UpgraderTools upgraderTools(List<UpgradeTask> tasks) {
        UpgraderTools upgraderTools = new UpgraderTools(tasks);
        upgraderTools.setSortByClassName(false);
        Collections.sort(tasks, (o1, o2) -> o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName()));

        if (jdbcTemplate != null) {
            upgraderTools.addUpgraderTracker("db", new DatabaseUpgraderTracker(jdbcTemplate));
        }
        upgraderTools.addUpgraderTracker(MongoDbUpgraderConstants.TRACKER, new MongoDbUpgraderTracker(mongoClient, databaseName));
        return upgraderTools;
    }

}
