/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.plugin.PluginResourceColumnSearch;

@Service
public interface PluginResourceColumnSearchDao extends JpaRepository<PluginResourceColumnSearch, Long> {

    void deleteByPluginResourceId(long pluginResourceId);

    List<PluginResourceColumnSearch> findAllByPluginResourceTypeAndColumnName(String resourceType, String columnName);

    @Query("SELECT DISTINCT s.columnName FROM PluginResourceColumnSearch s WHERE s.pluginResource.type = :resourceType ORDER BY s.columnName")
    List<String> findAllColumnNamesByResourceType(@Param("resourceType") String resourceType);

}
