/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.db.dao;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.plugin.PluginResourceLink;

@Service
public interface PluginResourceLinkDao extends JpaRepository<PluginResourceLink, Long> {

    long countByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(long fromPluginResourceId, String linkType, long toPluginResourceId);

    void deleteByFromPluginResourceId(long id);

    int deleteByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(long fromPluginResourceId, String linkType, long toPluginResourceId);

    @Modifying
    @Query("DELETE FROM PluginResourceLink WHERE fromPluginResource.id = :pluginResourceId OR toPluginResource.id = :pluginResourceId")
    void deleteByPluginResourceId(@Param("pluginResourceId") long pluginResourceId);

    List<PluginResourceLink> findAllByFromPluginResourceId(long fromPluginResourceId);

    List<PluginResourceLink> findAllByFromPluginResourceIdAndLinkType(long fromPluginResourceId, String linkType);

    List<PluginResourceLink> findAllByFromPluginResourceIdAndLinkTypeAndToPluginResourceType(long fromPluginResourceId, String linkType, String toResourceType);

    List<PluginResourceLink> findAllByFromPluginResourceTypeAndLinkTypeAndToPluginResourceId(String fromResourceType, String linkType, long toPluginResourceId);

    List<PluginResourceLink> findAllByLinkTypeAndToPluginResourceId(String linkType, long toPluginResourceId);

    @Query("SELECT prl FROM PluginResourceLink prl WHERE fromPluginResource.id IN :pluginResourceIds OR toPluginResource.id IN :pluginResourceIds")
    List<PluginResourceLink> findAllByPluginResourceId(@Param("pluginResourceIds") Collection<Long> pluginResourceIds);

    @Query("SELECT prl FROM PluginResourceLink prl WHERE fromPluginResource.id = :pluginResourceId OR toPluginResource.id = :pluginResourceId")
    List<PluginResourceLink> findAllByPluginResourceId(@Param("pluginResourceId") long pluginResourceId);

    List<PluginResourceLink> findAllByToPluginResourceId(long toPluginResourceId);

    PluginResourceLink findByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(long fromId, String linkType, long toId);

}
