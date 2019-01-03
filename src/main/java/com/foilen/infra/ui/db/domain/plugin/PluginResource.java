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
import javax.persistence.Lob;
import javax.persistence.Version;

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

/**
 * A Plugin resource.
 */
@Entity
public class PluginResource implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Version
    private long version;

    private String editorName;

    @Column(nullable = false)
    private String type;
    @Lob
    @Column(nullable = false)
    private String valueJson;

    public PluginResource() {
    }

    public PluginResource(String resourceType, IPResource resource) {
        store(resourceType, resource);
    }

    public String getEditorName() {
        return editorName;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValueJson() {
        return valueJson;
    }

    public long getVersion() {
        return version;
    }

    public <T extends IPResource> T loadResource(Class<T> loadingClass) {
        T resource = JsonTools.readFromString(valueJson, loadingClass);
        resource.setInternalId(id);
        resource.setResourceEditorName(editorName);
        return resource;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValueJson(String valueJson) {
        this.valueJson = valueJson;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void store(String resourceType, IPResource resource) {
        AssertTools.assertNotNull(resourceType, "The resourceType cannot be null");
        AssertTools.assertNotNull(resource, "The resource to store cannot be null");
        type = resourceType;
        valueJson = JsonTools.compactPrint(resource);
        editorName = resource.getResourceEditorName();
    }

}
