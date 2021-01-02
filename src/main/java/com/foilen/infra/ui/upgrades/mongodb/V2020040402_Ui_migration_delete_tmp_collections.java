/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.springframework.stereotype.Component;

@Component
public class V2020040402_Ui_migration_delete_tmp_collections extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {
        dropCollection("tmp_alert_to_send");
        dropCollection("tmp_api_machine_user");
        dropCollection("tmp_api_user");
        dropCollection("tmp_audit_item");
        dropCollection("tmp_machine_statistic_network");
        dropCollection("tmp_machine_statisticfs");
        dropCollection("tmp_machine_statistics");
        dropCollection("tmp_machine_statistics_fs");
        dropCollection("tmp_machine_statistics_networks");
        dropCollection("tmp_plugin_resource");
        dropCollection("tmp_plugin_resource_keep");
        dropCollection("tmp_plugin_resource_link");
        dropCollection("tmp_plugin_resource_tag");
        dropCollection("tmp_report_count");
        dropCollection("tmp_report_execution");
        dropCollection("tmp_report_time");
        dropCollection("tmp_user");
    }

}
