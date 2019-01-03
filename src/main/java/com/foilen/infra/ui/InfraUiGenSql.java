/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.foilen.smalltools.tools.FileTools;

public class InfraUiGenSql {

    public static void generateSqlSchema(Class<? extends Dialect> dialect, String outputSqlFile, boolean useUnderscore, String... packagesToScan) {

        BootstrapServiceRegistry bootstrapServiceRegistry = new BootstrapServiceRegistryBuilder().build();

        MetadataSources metadataSources = new MetadataSources(bootstrapServiceRegistry);

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Embeddable.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));
        for (String pkg : packagesToScan) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(pkg)) {
                metadataSources.addAnnotatedClassName(beanDefinition.getBeanClassName());
            }
        }

        StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder(bootstrapServiceRegistry);
        standardServiceRegistryBuilder.applySetting(AvailableSettings.DIALECT, dialect.getName());
        StandardServiceRegistryImpl ssr = (StandardServiceRegistryImpl) standardServiceRegistryBuilder.build();
        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder(ssr);

        if (useUnderscore) {
            metadataBuilder.applyImplicitNamingStrategy(new SpringImplicitNamingStrategy());
            metadataBuilder.applyPhysicalNamingStrategy(new SpringPhysicalNamingStrategy());
        }

        new SchemaExport((MetadataImplementor) metadataBuilder.build()) //
                .setHaltOnError(true) //
                .setOutputFile(outputSqlFile) //
                .setFormat(true) //
                .setDelimiter(";") //
                .execute(true, false, false, true);

    }

    public static void main(String[] args) {
        System.setProperty("hibernate.dialect.storage_engine", "innodb");
        FileTools.deleteFile("sql/mysql.sql");
        generateSqlSchema(MySQL5InnoDBDialect.class, "sql/mysql.sql", true, "com.foilen.infra.ui.db.domain");
    }

}
