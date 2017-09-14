/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.db.domain.monitoring;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.collect.ComparisonChain;

@Entity
public class MachineStatisticFS implements Comparable<MachineStatisticFS>, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(length = 2000)
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
        return ComparisonChain.start() //
                .compare(path, o.path) //
                .result();

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
