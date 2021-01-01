/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.visual;

import java.util.ArrayList;
import java.util.List;

public class MenuEntry {

    private String name;
    private String uri;
    private List<String> uriStartsWith = new ArrayList<>();

    private List<MenuEntry> children = new ArrayList<>();

    public MenuEntry addChild(String name) {
        MenuEntry menuEntry = new MenuEntry();
        menuEntry.setName(name);
        children.add(menuEntry);
        return menuEntry;
    }

    public MenuEntry addUriStartsWith(String uriStart) {
        uriStartsWith.add(uriStart);
        return this;
    }

    public List<MenuEntry> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public List<String> getUriStartsWith() {
        return uriStartsWith;
    }

    public MenuEntry setName(String name) {
        this.name = name;
        return this;
    }

    public MenuEntry setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MenuEntry [name=");
        builder.append(name);
        builder.append(", uri=");
        builder.append(uri);
        builder.append(", children size=");
        builder.append(children.size());
        builder.append("]");
        return builder.toString();
    }

}
