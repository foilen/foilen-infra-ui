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
import javax.persistence.Version;

/**
 * To help search values per column.
 */
@Entity
public class PluginResourceLink implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_plugin_resource_id", nullable = false)
    private PluginResource fromPluginResource;
    @Column(nullable = false)
    private String linkType;
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_plugin_resource_id", nullable = false)
    private PluginResource toPluginResource;

    public PluginResourceLink() {
    }

    public PluginResourceLink(PluginResource fromPluginResource, String linkType, PluginResource toPluginResource) {
        this.fromPluginResource = fromPluginResource;
        this.linkType = linkType;
        this.toPluginResource = toPluginResource;
    }

    public PluginResource getFromPluginResource() {
        return fromPluginResource;
    }

    public Long getId() {
        return id;
    }

    public String getLinkType() {
        return linkType;
    }

    public PluginResource getToPluginResource() {
        return toPluginResource;
    }

    public long getVersion() {
        return version;
    }

    public void setFromPluginResource(PluginResource fromPluginResource) {
        this.fromPluginResource = fromPluginResource;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public void setToPluginResource(PluginResource toPluginResource) {
        this.toPluginResource = toPluginResource;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
