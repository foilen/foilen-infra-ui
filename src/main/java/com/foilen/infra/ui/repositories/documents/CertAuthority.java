/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories.documents;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The types of certificate authority.
 */
@Document
public class CertAuthority {

    @Id
    private String id;
    @Version
    private long version;

    private String name;
    private String publicKeyText;
    private String privateKeyText;
    private String certificateText;
    private Date startDate;
    private Date endDate;

    public CertAuthority() {
    }

    public CertAuthority(String name, String publicKeyText, String privateKeyText, String certificateText, Date startDate, Date endDate) {
        this.name = name;
        this.publicKeyText = publicKeyText;
        this.privateKeyText = privateKeyText;
        this.certificateText = certificateText;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCertificateText() {
        return this.certificateText;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return this.name;
    }

    public String getPrivateKeyText() {
        return this.privateKeyText;
    }

    public String getPublicKeyText() {
        return this.publicKeyText;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setCertificateText(String certificateText) {
        this.certificateText = certificateText;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrivateKeyText(String privateKeyText) {
        this.privateKeyText = privateKeyText;
    }

    public void setPublicKeyText(String publicKeyText) {
        this.publicKeyText = publicKeyText;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CertAuthority [name=");
        builder.append(name);
        builder.append(", startDate=");
        builder.append(startDate);
        builder.append(", endDate=");
        builder.append(endDate);
        builder.append("]");
        return builder.toString();
    }

}
