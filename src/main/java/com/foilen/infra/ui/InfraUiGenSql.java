/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

import org.hibernate.dialect.MySQL5Dialect;

import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.Hibernate52Tools;

public class InfraUiGenSql {

    private static final String SQL_FILE = "sql/mysql.sql";

    public static void main(String[] args) {

        System.setProperty("hibernate.dialect.storage_engine", "innodb");
        FileTools.deleteFile(SQL_FILE);
        Hibernate52Tools.generateSqlSchema(MySQL5Dialect.class, SQL_FILE, true, "com.foilen.infra.ui.db.domain");
    }

}
