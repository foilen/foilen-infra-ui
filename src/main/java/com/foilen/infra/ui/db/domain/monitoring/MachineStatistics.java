/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.monitoring;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Machine's statistics.
 */
@Entity
public class MachineStatistics implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long machineInternalId;

    private Date timestamp;

    private long cpuUsed;
    private long cpuTotal;

    private long memoryUsed;
    private long memoryTotal;

    private long memorySwapUsed;
    private long memorySwapTotal;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<MachineStatisticFS> fs = new TreeSet<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<MachineStatisticNetwork> networks = new TreeSet<>();

    // Aggregation
    private int aggregationsForHour = 0;
    private int aggregationsForDay = 0;

    public MachineStatistics() {
    }

    public MachineStatistics(Long machineInternalId, Date timestamp, long cpuUsed, long cpuTotal, long memoryUsed, long memoryTotal, long memorySwapUsed, long memorySwapTotal) {
        this.machineInternalId = machineInternalId;
        this.timestamp = timestamp;
        this.cpuUsed = cpuUsed;
        this.cpuTotal = cpuTotal;
        this.memoryUsed = memoryUsed;
        this.memoryTotal = memoryTotal;
        this.memorySwapUsed = memorySwapUsed;
        this.memorySwapTotal = memorySwapTotal;
    }

    public int getAggregationsForDay() {
        return aggregationsForDay;
    }

    public int getAggregationsForHour() {
        return aggregationsForHour;
    }

    public long getCpuTotal() {
        return cpuTotal;
    }

    public long getCpuUsed() {
        return cpuUsed;
    }

    public Set<MachineStatisticFS> getFs() {
        return fs;
    }

    public Long getId() {
        return id;
    }

    public Long getMachineInternalId() {
        return machineInternalId;
    }

    public long getMemorySwapTotal() {
        return memorySwapTotal;
    }

    public long getMemorySwapUsed() {
        return memorySwapUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public Set<MachineStatisticNetwork> getNetworks() {
        return networks;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setAggregationsForDay(int aggregationsForDay) {
        this.aggregationsForDay = aggregationsForDay;
    }

    public void setAggregationsForHour(int aggregationsForHour) {
        this.aggregationsForHour = aggregationsForHour;
    }

    public void setCpuTotal(long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public void setCpuUsed(long cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public void setFs(Set<MachineStatisticFS> fs) {
        this.fs = fs;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMachineInternalId(Long machineInternalId) {
        this.machineInternalId = machineInternalId;
    }

    public void setMemorySwapTotal(long memorySwapTotal) {
        this.memorySwapTotal = memorySwapTotal;
    }

    public void setMemorySwapUsed(long memorySwapUsed) {
        this.memorySwapUsed = memorySwapUsed;
    }

    public void setMemoryTotal(long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public void setNetworks(Set<MachineStatisticNetwork> networks) {
        this.networks = networks;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MachineStatistics [machineInternalId=");
        builder.append(machineInternalId);
        builder.append(", timestamp=");
        builder.append(timestamp);
        builder.append(", cpuUsed=");
        builder.append(cpuUsed);
        builder.append(", cpuTotal=");
        builder.append(cpuTotal);
        builder.append(", memoryUsed=");
        builder.append(memoryUsed);
        builder.append(", memoryTotal=");
        builder.append(memoryTotal);
        builder.append(", memorySwapUsed=");
        builder.append(memorySwapUsed);
        builder.append(", memorySwapTotal=");
        builder.append(memorySwapTotal);
        builder.append(", fs=");
        builder.append(fs);
        builder.append(", networks=");
        builder.append(networks);
        builder.append(", aggregationsForHour=");
        builder.append(aggregationsForHour);
        builder.append(", aggregationsForDay=");
        builder.append(aggregationsForDay);
        builder.append("]");
        return builder.toString();
    }

}
