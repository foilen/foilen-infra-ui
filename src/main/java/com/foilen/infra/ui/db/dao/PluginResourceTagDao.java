/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.plugin.PluginResourceTag;

@Service
public interface PluginResourceTagDao extends JpaRepository<PluginResourceTag, Long> {

    void deleteByPluginResourceId(long internalId);

    int deleteByPluginResourceIdAndTagName(long internalId, String tagName);

    @Query("SELECT t.tagName FROM PluginResourceTag t WHERE t.pluginResource.id = :internalId")
    Set<String> findAllTagNameByPluginResourceId(@Param("internalId") Long internalId);

    PluginResourceTag findByPluginResourceIdAndTagName(long pluginResourceId, String tagName);

}
