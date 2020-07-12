/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.List;

import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;

public interface PluginResourceInUiCustomRepository {

    List<PluginResource> findAllWithoutOwner();

    void updateOwner(String internalId, String owner);

}
