/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.base.Strings;

@Configuration
@ComponentScan({ "com.foilen.infra.ui.upgrades.mysql" })
public class InfraUiUpgradesMysqlSpringConfig {

    @Value("${infraUi.mysqlHostName}")
    private String mysqlHostName;
    @Value("${infraUi.mysqlPort}")
    private int mysqlPort;
    @Value("${infraUi.mysqlDatabaseName}")
    private String mysqlDatabaseName;
    @Value("${infraUi.mysqlDatabasePassword}")
    private String mysqlDatabasePassword;
    @Value("${infraUi.mysqlDatabaseUserName}")
    private String mysqlDatabaseUserName;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(mariadbDataSource());
    }

    @Bean
    public DataSource mariadbDataSource() {
        MariaDbDataSource dataSource = new MariaDbDataSource(mysqlHostName, mysqlPort, mysqlDatabaseName);
        try {
            if (!Strings.isNullOrEmpty(mysqlDatabaseUserName)) {
                dataSource.setUserName(mysqlDatabaseUserName);
            }
            if (!Strings.isNullOrEmpty(mysqlDatabasePassword)) {
                dataSource.setPassword(mysqlDatabasePassword);
            }
        } catch (SQLException e) {
            throw new InfraUiException("Cannot connect to MariaDB", e);
        }

        return dataSource;
    }

}
