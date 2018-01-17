/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.monitoring;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.google.common.collect.ComparisonChain;

@Entity
public class MachineStatisticNetwork implements Comparable<MachineStatisticNetwork>, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String interfaceName;

    private long inBytes;
    private long outBytes;

    public MachineStatisticNetwork() {
    }

    public MachineStatisticNetwork(String interfaceName, long inBytes, long outBytes) {
        this.interfaceName = interfaceName;
        this.inBytes = inBytes;
        this.outBytes = outBytes;
    }

    @Override
    public int compareTo(MachineStatisticNetwork o) {
        return ComparisonChain.start() //
                .compare(interfaceName, o.interfaceName) //
                .result();

    }

    public long getInBytes() {
        return inBytes;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public void setInBytes(long inBytes) {
        this.inBytes = inBytes;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setOutBytes(long outBytes) {
        this.outBytes = outBytes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MachineStatisticNetwork [interfaceName=");
        builder.append(interfaceName);
        builder.append(", inBytes=");
        builder.append(inBytes);
        builder.append(", outBytes=");
        builder.append(outBytes);
        builder.append("]");
        return builder.toString();
    }

}
