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
 * A signed certificate.
 */
@Document
public class CertNode {

    @Id
    private String id;
    @Version
    private long version;

    private String certAuthorityName;
    private String commonName;

    private String certAuthorityId;
    private String publicKeyText;
    private String privateKeyText;
    private String certificateText;
    private Date startDate;
    private Date endDate;

    public CertNode() {
    }

    public CertNode(CertAuthority certAuthority, String commonName, String publicKeyText, String privateKeyText, String certificateText, Date startDate, Date endDate) {
        this.certAuthorityName = certAuthority.getName();
        this.certAuthorityId = certAuthority.getId();
        this.commonName = commonName;
        this.publicKeyText = publicKeyText;
        this.privateKeyText = privateKeyText;
        this.certificateText = certificateText;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CertNode(String certAuthorityId, String certAuthorityName, String commonName, String publicKeyText, String privateKeyText, String certificateText, Date startDate, Date endDate) {
        this.certAuthorityId = certAuthorityId;
        this.certAuthorityName = certAuthorityName;
        this.commonName = commonName;
        this.publicKeyText = publicKeyText;
        this.privateKeyText = privateKeyText;
        this.certificateText = certificateText;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCertAuthorityId() {
        return certAuthorityId;
    }

    public String getCertAuthorityName() {
        return certAuthorityName;
    }

    public String getCertificateText() {
        return this.certificateText;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public String getId() {
        return id;
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

    public void setCertAuthorityId(String certAuthorityId) {
        this.certAuthorityId = certAuthorityId;
    }

    public void setCertAuthorityName(String certAuthorityName) {
        this.certAuthorityName = certAuthorityName;
    }

    public void setCertificateText(String certificateText) {
        this.certificateText = certificateText;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setId(String id) {
        this.id = id;
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
        builder.append("CertNode [name=");
        builder.append(commonName);
        builder.append(", startDate=");
        builder.append(startDate);
        builder.append(", endDate=");
        builder.append(endDate);
        builder.append("]");
        return builder.toString();
    }

}
