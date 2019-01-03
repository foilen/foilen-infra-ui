/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.ui.db.domain.plugin.PluginResource;

public interface ResourceManagementService {

    void changesExecute(ChangesContext changesContext, ResponseResourceAppliedChanges responseResourceAppliedChanges);

    void updateColumnSearches(PluginResource pluginResource);

}
