/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents.models;

import com.foilen.smalltools.tools.StringTools;

public class MachineStatisticFS implements Comparable<MachineStatisticFS> {

    private String path;
    private boolean isRoot;

    private long usedSpace;
    private long totalSpace;

    public MachineStatisticFS() {
    }

    public MachineStatisticFS(String path, boolean isRoot, long usedSpace, long totalSpace) {
        this.path = path;
        this.isRoot = isRoot;
        this.usedSpace = usedSpace;
        this.totalSpace = totalSpace;
    }

    @Override
    public int compareTo(MachineStatisticFS o) {
        return StringTools.safeComparisonNullFirst(path, o.path);
    }

    public String getPath() {
        return path;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MachineStatisticFS [path=");
        builder.append(path);
        builder.append(", isRoot=");
        builder.append(isRoot);
        builder.append(", usedSpace=");
        builder.append(usedSpace);
        builder.append(", totalSpace=");
        builder.append(totalSpace);
        builder.append("]");
        return builder.toString();
    }

}
