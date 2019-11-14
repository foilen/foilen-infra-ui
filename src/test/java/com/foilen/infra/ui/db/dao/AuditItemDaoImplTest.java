/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.foilen.infra.apitmp.request.RequestAuditItem;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.AuditUserType;
import com.foilen.infra.resource.example.JunitResource;
import com.foilen.infra.ui.db.domain.audit.AuditItem;
import com.foilen.infra.ui.services.AuditingService;
import com.foilen.infra.ui.services.PaginationServiceImpl;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.JsonTools;

public class AuditItemDaoImplTest extends AbstractSpringTests {

    @Autowired
    private AuditItemDao auditItemDao;

    @Autowired
    private AuditingService auditingService;
    @Autowired
    private PaginationServiceImpl paginationServiceImpl;

    public AuditItemDaoImplTest() {
        super(false);
    }

    @Before
    public void createAuditData() {
        paginationServiceImpl.setItemsPerPage(100);

        auditingService.resourceAdd("tx1", true, AuditUserType.USER, "12345", new JunitResource("r1"));
        auditingService.resourceAdd("tx1", false, AuditUserType.USER, "12345", new JunitResource("r2"));

        auditingService.resourceAdd("tx2", true, AuditUserType.SYSTEM, null, new JunitResource("r3"));
        auditingService.resourceUpdate("tx2", false, AuditUserType.SYSTEM, null, new JunitResource("r1"), new JunitResource("r1", null, 2));

        auditItemDao.findAllByTxId("tx1", null).forEach(a -> {
            a.setTimestamp(DateTools.parseFull("2000-01-01 01:00:00"));
            auditItemDao.save(a);
        });
        auditItemDao.findAllByTxId("tx2", null).forEach(a -> {
            a.setTimestamp(DateTools.parseFull("2000-05-01 01:00:00"));
            auditItemDao.save(a);
        });
    }

    private Map<String, Object> reorder(Page<AuditItem> items) {
        Optional<Long> lowest = auditItemDao.findAll().stream().map(i -> i.getId()).min((a, b) -> Long.compare(a, b));
        if (lowest.isPresent()) {
            long sub = lowest.get();
            items.forEach(i -> i.setId(i.getId() - sub));
        }

        @SuppressWarnings("unchecked")
        TreeMap<String, Object> map = JsonTools.clone(items, TreeMap.class);
        map.remove("pageable");
        return map;
    }

    @Test
    public void testFindAllRequestAuditItem_all() {
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all.json", getClass(), reorder(auditItemDao.findAll(new RequestAuditItem())));
    }

    @Test
    public void testFindAllRequestAuditItem_all_p1() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all_p1.json", getClass(), reorder(auditItemDao.findAll(new RequestAuditItem().setPageId(1))));
    }

    @Test
    public void testFindAllRequestAuditItem_all_p2() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_all_p2.json", getClass(), reorder(auditItemDao.findAll(new RequestAuditItem().setPageId(2))));
    }

    @Test
    public void testFindAllRequestAuditItem_explicit_p1() {
        paginationServiceImpl.setItemsPerPage(2);
        AssertTools.assertJsonComparisonWithoutNulls("testFindAllRequestAuditItem_explicit_p1.json", getClass(), reorder(auditItemDao.findAll(new RequestAuditItem().setPageId(1) //
                .setExplicitChange(true))));
    }

}
