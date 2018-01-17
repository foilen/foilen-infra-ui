/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.core.system.common.service.IPPluginServiceImpl;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.domain.plugin.PluginResource;
import com.foilen.infra.ui.services.ResourceManagementService;

@Component
public class IPPluginServiceUiImpl extends IPPluginServiceImpl {

    @Autowired
    private PluginResourceDao pluginResourceDao;
    @Autowired
    private PluginResourceColumnSearchDao pluginResourceColumnSearchDao;
    @Autowired
    private ResourceManagementService resourceManagementService;

    @Override
    protected void updateResourcesColumnSearch(List<IPResourceDefinition> resourceDefinitions) {

        for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
            String resourceType = resourceDefinition.getResourceType();

            logger.info("[{}] Checking column search indexes", resourceType);

            // Check which columns are currently indexed
            List<String> existingColumns = pluginResourceColumnSearchDao.findAllColumnNamesByResourceType(resourceType);
            logger.info("[{}] Currently indexed columns [{}]", resourceType, existingColumns);

            List<String> expectedColumns = resourceDefinition.getSearchableProperties().stream() //
                    .sorted() //
                    .collect(Collectors.toList());
            logger.info("[{}] Expected indexed columns [{}]", resourceType, expectedColumns);

            // If missing columns, process all with pluginResourceService (and ignore next steps)
            boolean missingColumns = false;
            for (String expectedColumn : expectedColumns) {
                if (!existingColumns.contains(expectedColumn)) {
                    missingColumns = true;
                    break;
                }
            }
            if (missingColumns) {
                logger.info("[{}] Missing some columns. Will process all the items of that type", resourceType);
                for (PluginResource pluginResource : pluginResourceDao.findAllByType(resourceType)) {
                    resourceManagementService.updateColumnSearches(pluginResource);
                }
                continue;
            }

            // If extra column, delete the extra data
            for (String existingColumn : existingColumns) {
                if (!expectedColumns.contains(existingColumn)) {
                    logger.info("[{}] Extra column [{}]. Deleting", resourceType, existingColumn);
                    pluginResourceColumnSearchDao.deleteInBatch( //
                            pluginResourceColumnSearchDao.findAllByPluginResourceTypeAndColumnName(resourceType, existingColumn) //
                    );
                }
            }

        }

    }
}
