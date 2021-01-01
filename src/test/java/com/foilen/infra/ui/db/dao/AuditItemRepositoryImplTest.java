/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.foilen.infra.api.request.RequestAuditItem;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.resource.example.JunitResource;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.JsonTools;

public class AuditItemRepositoryImplTest extends AbstractSpringTests {

    @Autowired
    private AuditItemRepository auditItemRepository;
    @Autowired
    private AuditingService auditingService;

    public AuditItemRepositoryImplTest() {
        super(false);
    }

    @Before
    public void createAuditData() {
        auditingService.resourceAdd("tx1", true, AuditUserType.USER, "12345", new JunitResource("r1"));
        auditingService.resourceAdd("tx1", false, AuditUserType.USER, "12345", new JunitResource("r2"));

        auditingService.resourceAdd("tx2", true, AuditUserType.SYSTEM, null, new JunitResource("r3"));
        auditingService.resourceUpdate("tx2", false, AuditUserType.SYSTEM, null, new JunitResource("r1"), new JunitResource("r1", null, 2));

        auditItemRepository.findAllByTxId("tx1", null).forEach(a -> {
            a.setTimestamp(DateTools.parseFull("2000-01-01 01:00:00"));
            auditItemRepository.save(a);
        });
        auditItemRepository.findAllByTxId("tx2", null).forEach(a -> {
            a.setTimestamp(DateTools.parseFull("2000-05-01 01:00:00"));
            auditItemRepository.save(a);
        });
    }

    private Map<String, Object> reorder(Page<AuditItem> items) {
        AtomicInteger id = new AtomicInteger();
        items.stream() //
                .sorted((a, b) -> a.getId().compareTo(b.getId())) //
                .peek(i -> i.setId(String.valueOf(id.getAndIncrement()))) //
                .sorted((a, b) -> Integer.valueOf(b.getId()).compareTo(Integer.valueOf(a.getId()))) //
                .collect(Collectors.toList());

        @SuppressWarnings("unchecked")
        TreeMap<String, Object> map = JsonTools.clone(items, TreeMap.class);
        map.remove("pageable");
        return map;
    }

    @Test
    public void testFindAllRequestAuditItem_all() {
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all.json", getClass(), reorder(auditItemRepository.findAll(new RequestAuditItem())));
    }

    @Test
    public void testFindAllRequestAuditItem_all_p1() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all_p1.json", getClass(), reorder(auditItemRepository.findAll(new RequestAuditItem().setPageId(1))));
    }

    @Test
    public void testFindAllRequestAuditItem_all_p2() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all_p2.json", getClass(), reorder(auditItemRepository.findAll(new RequestAuditItem().setPageId(2))));
    }

    @Test
    public void testFindAllRequestAuditItem_explicit_p1() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_explicit_p1.json", getClass(), reorder(auditItemRepository.findAll(new RequestAuditItem().setPageId(1) //
                .setExplicitChange(true))));
    }

}
