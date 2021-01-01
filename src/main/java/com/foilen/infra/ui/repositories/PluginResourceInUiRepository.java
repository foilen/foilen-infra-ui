/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;

public interface PluginResourceInUiRepository extends MongoRepository<PluginResourceRepository, String>, PluginResourceInUiCustomRepository {

}
