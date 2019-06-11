/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.db.dao.AuditItemDao;
import com.foilen.infra.ui.db.domain.audit.AuditAction;
import com.foilen.infra.ui.db.domain.audit.AuditItem;
import com.foilen.infra.ui.db.domain.audit.AuditType;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;

@Service
@Transactional
public class AuditingServiceImpl extends AbstractBasics implements AuditingService {

    @Autowired
    private AuditItemDao auditItemDao;
    @Autowired
    private IPResourceService ipResourceService;

    private AuditItem createAuditItem(String txId, boolean explicitChange, //
            AuditType auditType, AuditAction auditAction, //
            AuditUserType userType, String userName) {

        AuditItem auditItem = new AuditItem();
        auditItem.setTxId(txId);
        auditItem.setExplicitChange(explicitChange);

        auditItem.setType(auditType.name());
        auditItem.setAction(auditAction.name());

        auditItem.setUserType(userType.name());
        auditItem.setUserName(userName);
        return auditItem;
    }

    @Override
    public Page<AuditItem> findAllByTxId(String txId, int pageId, int itemsPerPage) {
        return auditItemDao.findAllByTxId(txId, PageRequest.of(pageId, itemsPerPage, Direction.ASC, "id"));
    }

    @Override
    public void linkAdd(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource fromResource, String linkType, IPResource toResource) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.LINK, AuditAction.ADD, userType, userName);
        setResources(auditItem, fromResource, toResource);
        auditItem.setLinkType(linkType);
        auditItemDao.save(auditItem);
    }

    @Override
    public void linkDelete(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource fromResource, String linkType, IPResource toResource) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.LINK, AuditAction.DELETE, userType, userName);
        setResources(auditItem, fromResource, toResource);
        auditItem.setLinkType(linkType);
        auditItemDao.save(auditItem);
    }

    @Override
    public void resourceAdd(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource resource) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.RESOURCE, AuditAction.ADD, userType, userName);
        setResources(auditItem, resource);
        auditItemDao.save(auditItem);
    }

    @Override
    public void resourceDelete(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource resource) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.RESOURCE, AuditAction.DELETE, userType, userName);
        setResources(auditItem, resource);
        auditItemDao.save(auditItem);
    }

    @Override
    public void resourceUpdate(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource beforeResource, IPResource afterResource) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.RESOURCE, AuditAction.UPDATE, userType, userName);
        setResources(auditItem, beforeResource, afterResource);
        auditItemDao.save(auditItem);
    }

    private void setResources(AuditItem auditItem, IPResource resourceFirst) {
        setResources(auditItem, resourceFirst, null);
    }

    private void setResources(AuditItem auditItem, IPResource resourceFirst, IPResource resourceSecond) {
        if (resourceFirst != null) {
            auditItem.setResourceFirstType(ipResourceService.getResourceDefinition(resourceFirst.getClass()).getResourceType());
            auditItem.setResourceFirst(JsonTools.prettyPrint(resourceFirst));
        }
        if (resourceSecond != null) {
            auditItem.setResourceSecondType(ipResourceService.getResourceDefinition(resourceSecond.getClass()).getResourceType());
            auditItem.setResourceSecond(JsonTools.prettyPrint(resourceSecond));
        }
    }

    @Override
    public void tagAdd(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource resource, String tagName) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.TAG, AuditAction.ADD, userType, userName);
        setResources(auditItem, resource);
        auditItem.setTagName(tagName);
        auditItemDao.save(auditItem);
    }

    @Override
    public void tagDelete(String txId, boolean explicitChange, AuditUserType userType, String userName, IPResource resource, String tagName) {
        AuditItem auditItem = createAuditItem(txId, explicitChange, AuditType.TAG, AuditAction.DELETE, userType, userName);
        auditItem.setResourceFirst(JsonTools.prettyPrint(resource));
        auditItem.setTagName(tagName);
        auditItemDao.save(auditItem);
    }

}
