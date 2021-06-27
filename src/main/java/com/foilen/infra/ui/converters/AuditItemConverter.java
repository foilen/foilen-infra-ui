/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.audit.AuditAction;
import com.foilen.infra.api.model.audit.AuditType;
import com.foilen.infra.api.model.resource.ResourceDetails;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class AuditItemConverter extends AbstractBasics implements Converter<AuditItem, com.foilen.infra.api.model.audit.AuditItem> {

    @Override
    public com.foilen.infra.api.model.audit.AuditItem convert(AuditItem source) {
        com.foilen.infra.api.model.audit.AuditItem target = new com.foilen.infra.api.model.audit.AuditItem();

        target.setId(source.getId());
        target.setTimestamp(source.getTimestamp());
        target.setTxId(source.getTxId());
        target.setExplicitChange(source.isExplicitChange());
        target.setType(AuditType.valueOf(source.getType()));
        target.setAction(AuditAction.valueOf(source.getAction()));
        target.setUserType(source.getUserType());
        target.setUserName(source.getUserName());
        target.setImpersonatorUserType(source.getImpersonatorUserType());
        target.setImpersonatorUserName(source.getImpersonatorUserName());
        if (source.getResourceFirstType() != null) {
            target.setResourceFirst(toResourceDetails(source.getResourceFirstType(), source.getResourceFirst()));
        }
        if (source.getResourceSecondType() != null) {
            target.setResourceSecond(toResourceDetails(source.getResourceSecondType(), source.getResourceSecond()));
        }
        target.setLinkType(source.getLinkType());
        target.setTagName(source.getTagName());

        target.setDocumentType(source.getDocumentType());
        target.setDocumentId(source.getDocumentId());
        target.setDocumentFrom(source.getDocumentFrom());
        target.setDocumentTo(source.getDocumentTo());

        return target;
    }

    private ResourceDetails toResourceDetails(String resourceType, Object resource) {
        String resourceId = null;

        if (resource instanceof IPResource) {
            IPResource ipResource = (IPResource) resource;
            resourceId = ipResource.getInternalId();
        }

        ResourceDetails resourceDetails = new ResourceDetails(resourceId, resourceType, resource);
        return resourceDetails;
    }

}
