/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.plugin.PluginResource;

@Service
public interface PluginResourceDao extends JpaRepository<PluginResource, Long> {

    long deleteOneById(long id);

    List<PluginResource> findAllByIdIn(Collection<Long> ids);

    Set<PluginResource> findAllByType(String type);

}
