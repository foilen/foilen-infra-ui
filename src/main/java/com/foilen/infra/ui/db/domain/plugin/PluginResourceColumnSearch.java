/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.plugin;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

/**
 * To help search values per column.
 */
@Entity
public class PluginResourceColumnSearch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plugin_resource_id", nullable = false)
    private PluginResource pluginResource;
    @Column(nullable = false)
    private String columnName;

    @Column
    private Boolean bool;
    @Column(length = 4000)
    private String text;
    @Column
    private Long longNumber;
    @Column
    private Integer intNumber;
    @Column
    private Float floatNumber;
    @Column
    private Double doubleNumber;

    public PluginResourceColumnSearch() {
    }

    public PluginResourceColumnSearch(PluginResource pluginResource, String columnName) {
        this.pluginResource = pluginResource;
        this.columnName = columnName;
    }

    public Boolean getBool() {
        return bool;
    }

    public String getColumnName() {
        return columnName;
    }

    public Double getDoubleNumber() {
        return doubleNumber;
    }

    public Float getFloatNumber() {
        return floatNumber;
    }

    public Long getId() {
        return id;
    }

    public Integer getIntNumber() {
        return intNumber;
    }

    public Long getLongNumber() {
        return longNumber;
    }

    public PluginResource getPluginResource() {
        return pluginResource;
    }

    public String getText() {
        return text;
    }

    public long getVersion() {
        return version;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setDoubleNumber(Double doubleNumber) {
        this.doubleNumber = doubleNumber;
    }

    public void setFloatNumber(Float floatNumber) {
        this.floatNumber = floatNumber;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIntNumber(Integer intNumber) {
        this.intNumber = intNumber;
    }

    public void setLongNumber(Long longNumber) {
        this.longNumber = longNumber;
    }

    public void setPluginResource(PluginResource pluginResource) {
        this.pluginResource = pluginResource;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
