/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.dns.DnsEntry;
import com.foilen.infra.resource.dns.ManualDnsEntryEditor;
import com.foilen.infra.resource.dns.model.DnsEntryType;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.JsonTools;

public class AuditingServiceImplTest extends AbstractSpringTests {

    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService resourceService;
    @Autowired
    private AuditItemRepository auditItemDao;

    public AuditingServiceImplTest() {
        super(false);
    }

    @Test
    public void test() {

        Sort sort = Sort.by("type", "action", "linkType", "resourceFirst.resourceType", "resourceSecond.resourceType", "id");

        // Add
        ChangesContext changes = new ChangesContext(resourceService);
        DnsEntry dnsEntry1 = new DnsEntry("d1.example.com", DnsEntryType.A, "127.0.0.1");
        DnsEntry dnsEntry2 = new DnsEntry("d2.example.com", DnsEntryType.A, "127.0.0.1");

        setResourceEditor(ManualDnsEntryEditor.EDITOR_NAME, dnsEntry1, dnsEntry2);

        changes.resourceAdd(dnsEntry1);
        changes.resourceAdd(dnsEntry2);
        changes.tagAdd(dnsEntry1, "super");
        changes.linkAdd(dnsEntry1, LinkTypeConstants.USES, dnsEntry2);
        internalChangeService.changesExecute(changes);

        // Assert Add
        List<AuditItem> items = auditItemDao.findAll(sort).stream() //
                .map(it -> {
                    AuditItem cloned = JsonTools.clone(it);
                    cloned.setId(null);
                    cloned.setTxId(null);
                    cloned.setTimestamp(null);
                    return cloned;
                }) //
                .collect(Collectors.toList());
        AssertTools.assertJsonComparisonWithoutNulls("AuditingServiceImplTest-add.json", getClass(), items);

        // Update
        auditItemDao.deleteAll();
        changes.clear();

        DnsEntry dnsEntry3 = new DnsEntry("d3.example.com", DnsEntryType.A, "127.0.0.1");
        changes.resourceUpdate(dnsEntry1.getInternalId(), dnsEntry3);
        internalChangeService.changesExecute(changes);

        // Assert Update
        items = auditItemDao.findAll(sort).stream() //
                .map(it -> {
                    AuditItem cloned = JsonTools.clone(it);
                    cloned.setId(null);
                    cloned.setTxId(null);
                    cloned.setTimestamp(null);
                    return cloned;
                }) //
                .collect(Collectors.toList());
        AssertTools.assertJsonComparisonWithoutNulls("AuditingServiceImplTest-update.json", getClass(), items);

        // Delete links and tags
        auditItemDao.deleteAll();
        changes.clear();

        changes.tagDelete(dnsEntry3, "super");
        changes.linkDelete(dnsEntry3, LinkTypeConstants.USES, dnsEntry2);
        internalChangeService.changesExecute(changes);

        // Assert Delete
        items = auditItemDao.findAll(sort).stream() //
                .map(it -> {
                    AuditItem cloned = JsonTools.clone(it);
                    cloned.setId(null);
                    cloned.setTxId(null);
                    cloned.setTimestamp(null);
                    return cloned;
                }) //
                .collect(Collectors.toList());
        AssertTools.assertJsonComparisonWithoutNulls("AuditingServiceImplTest-delete.json", getClass(), items);

    }

}
