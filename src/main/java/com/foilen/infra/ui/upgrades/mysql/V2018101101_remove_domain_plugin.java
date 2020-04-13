/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mysql;

import java.util.List;

import org.springframework.stereotype.Component;

import com.foilen.smalltools.upgrader.tasks.AbstractDatabaseUpgradeTask;

@Component
public class V2018101101_remove_domain_plugin extends AbstractDatabaseUpgradeTask {

    @Override
    public void execute() {

        logger.info("Starting to remove the Domain resources");

        long deleted = 0;

        for (;;) {
            List<Long> pluginResourceIds = jdbcTemplate.queryForList("SELECT id FROM plugin_resource WHERE type = 'Domain' LIMIT 100", Long.class);
            if (pluginResourceIds.isEmpty()) {
                break;
            }

            logger.info("Deleting {} Domain resources", pluginResourceIds.size());

            for (long pluginResourceId : pluginResourceIds) {
                jdbcTemplate.update("DELETE FROM plugin_resource_column_search WHERE plugin_resource_id = ?", pluginResourceId);
                jdbcTemplate.update("DELETE FROM plugin_resource_link WHERE from_plugin_resource_id = ? OR to_plugin_resource_id = ?", pluginResourceId, pluginResourceId);
                jdbcTemplate.update("DELETE FROM plugin_resource_tag WHERE plugin_resource_id = ?", pluginResourceId);
                jdbcTemplate.update("DELETE FROM plugin_resource WHERE id = ?", pluginResourceId);
            }

            deleted += pluginResourceIds.size();
        }

        logger.info("Removed {} Domain resources", deleted);
    }

    @Override
    public String useTracker() {
        return "db";
    }

}
