/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tagName", "plugin_resource_id" }))
public class PluginResourceTag implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @Column(nullable = false)
    private String tagName;
    @ManyToOne
    @JoinColumn(name = "plugin_resource_id", nullable = false)
    private PluginResource pluginResource;

    public PluginResourceTag() {
    }

    public PluginResourceTag(String tagName, PluginResource pluginResource) {
        this.tagName = tagName;
        this.pluginResource = pluginResource;
    }

    public Long getId() {
        return id;
    }

    public PluginResource getPluginResource() {
        return pluginResource;
    }

    public String getTagName() {
        return tagName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPluginResource(PluginResource pluginResource) {
        this.pluginResource = pluginResource;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

}
