/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.foilen.infra.ui.repositories.CertAuthorityRepository;
import com.foilen.infra.ui.repositories.CertNodeRepository;
import com.foilen.infra.ui.repositories.documents.CertAuthority;
import com.foilen.infra.ui.repositories.documents.CertNode;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.ResourceTools;

public class V2021081601_Ui_DeleteExtraCATest extends AbstractSpringTests {

    @Autowired
    private CertAuthorityRepository certAuthorityRepository;

    @Autowired
    private CertNodeRepository certNodeRepository;
    @Autowired
    private V2021081601_Ui_DeleteExtraCA task;

    public V2021081601_Ui_DeleteExtraCATest() {
        super(false);
    }

    private void assertCerts(String expectedResource, Map<String, String> idMappingCa, Map<String, String> idMappingNode) {

        List<CertAuthority> cas = certAuthorityRepository.findAll(Sort.by("name", "startDate"));
        List<CertNode> nodes = certNodeRepository.findAll(Sort.by("certAuthorityName", "startDate"));

        // Details of CA
        Map<String, String> detailsByCertId = new HashMap<>();
        Map<String, List<String>> nodesByCertId = new HashMap<>();
        cas.forEach(ca -> {
            String id = idMappingCa.get(ca.getId());
            if (id == null) {
                id = String.valueOf(idMappingCa.size());
                idMappingCa.put(ca.getId(), id);
            }
            String name = ca.getName();
            String startDate = DateTools.formatDateOnly(new Date(ca.getStartDate().getTime() - System.currentTimeMillis()));
            String endDate = DateTools.formatDateOnly(new Date(ca.getEndDate().getTime() - System.currentTimeMillis()));
            detailsByCertId.put(id, id + " " + name + " " + startDate + " - " + endDate);
            nodesByCertId.put(id, new ArrayList<>());
        });

        // Details of nodes
        nodes.forEach(node -> {
            String id = idMappingNode.get(node.getId());
            if (id == null) {
                id = String.valueOf(idMappingNode.size() + 1000);
                idMappingNode.put(node.getId(), id);
            }
            String name = node.getCertAuthorityName();
            String startDate = DateTools.formatDateOnly(new Date(node.getStartDate().getTime() - System.currentTimeMillis()));
            String endDate = DateTools.formatDateOnly(new Date(node.getEndDate().getTime() - System.currentTimeMillis()));
            nodesByCertId.get(idMappingCa.get(node.getCertAuthorityId())).add(id + " " + name + " " + startDate + " - " + endDate);

        });

        // Report
        StringBuilder actual = new StringBuilder();
        nodesByCertId.keySet().stream().sorted().forEach(certId -> {
            actual.append("\n").append(detailsByCertId.get(certId));
            nodesByCertId.get(certId).forEach(node -> {
                actual.append("\n\t").append(node);
            });
        });
        actual.append("\n");

        // Assert
        AssertTools.assertIgnoreLineFeed(ResourceTools.getResourceAsString(expectedResource, getClass()), actual.toString());

    }

    @Test
    public void testExecute() {

        Map<String, String> idMappingCa = new HashMap<>();
        Map<String, String> idMappingNode = new HashMap<>();

        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create many CA and nodes
        for (int i = 0; i < 5; ++i) {
            certAuthorityRepository.save(new CertAuthority("firstCA", "PrivateK", "PublicKey", "Cert", DateTools.addDate(Calendar.MONTH, i), DateTools.addDate(Calendar.MONTH, i + 3)));
            certAuthorityRepository.save(new CertAuthority("secondCA", "PrivateK", "PublicKey", "Cert", DateTools.addDate(Calendar.MONTH, i), DateTools.addDate(Calendar.MONTH, i + 3)));
        }

        String certAuthId = certAuthorityRepository.findAllByNameOrderByStartDate("firstCA").get(1).getId();
        certNodeRepository.save(new CertNode(certAuthId, "firstCA", "h1", null, null, null, new Date(), DateTools.addDate(Calendar.MONTH, 3)));
        certNodeRepository.save(new CertNode(certAuthId, "firstCA", "h2", null, null, null, new Date(), DateTools.addDate(Calendar.MONTH, 3)));
        certAuthId = certAuthorityRepository.findAllByNameOrderByStartDate("secondCA").get(3).getId();
        certNodeRepository.save(new CertNode(certAuthId, "secondCA", "h1", null, null, null, new Date(), DateTools.addDate(Calendar.MONTH, 3)));

        assertCerts("V2021081601_Ui_DeleteExtraCATest-testExecute-1.txt", idMappingCa, idMappingNode);

        // Cleanup
        task.execute();
        assertCerts("V2021081601_Ui_DeleteExtraCATest-testExecute-2.txt", idMappingCa, idMappingNode);

    }

}
