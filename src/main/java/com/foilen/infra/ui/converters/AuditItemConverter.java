/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.AuditAction;
import com.foilen.infra.api.model.AuditType;
import com.foilen.infra.api.model.ResourceDetails;
import com.foilen.infra.ui.db.domain.audit.AuditItem;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;

@Component
public class AuditItemConverter extends AbstractBasics implements Converter<AuditItem, com.foilen.infra.apitmp.model.AuditItem> {

    @Override
    public com.foilen.infra.apitmp.model.AuditItem convert(AuditItem source) {
        com.foilen.infra.apitmp.model.AuditItem target = new com.foilen.infra.apitmp.model.AuditItem();

        target.setId(source.getId());
        target.setTimestamp(source.getTimestamp());
        target.setTxId(source.getTxId());
        target.setExplicitChange(source.isExplicitChange());
        target.setType(AuditType.valueOf(source.getType()));
        target.setAction(AuditAction.valueOf(source.getAction()));
        target.setUserType(source.getUserType());
        target.setUserName(source.getUserName());
        if (source.getResourceFirstType() != null) {
            target.setResourceFirst(new ResourceDetails(source.getResourceFirstType(), JsonTools.readFromString(source.getResourceFirst(), Object.class)));
        }
        if (source.getResourceSecondType() != null) {
            target.setResourceSecond(new ResourceDetails(source.getResourceSecondType(), JsonTools.readFromString(source.getResourceSecond(), Object.class)));
        }
        target.setLinkType(source.getLinkType());
        target.setTagName(source.getTagName());

        return target;
    }

}
