/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

public class CertificateServiceImplTest extends AbstractSpringTests {

    @Autowired
    private CertAuthorityRepository certAuthorityRepository;
    @Autowired
    private CertNodeRepository certNodeRepository;
    @Autowired
    private CertificateService certificateService;

    public CertificateServiceImplTest() {
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
    public void testFindOrCreateAuthorityByName() {
        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create one
        List<CertAuthority> firstCall = certificateService.findOrCreateAuthorityByName("first");
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Get the same
        List<CertAuthority> secondCall = certificateService.findOrCreateAuthorityByName("first");
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());
        Assert.assertEquals( //
                firstCall.stream().map(CertAuthority::getName).collect(Collectors.toList()), //
                secondCall.stream().map(CertAuthority::getName).collect(Collectors.toList()));

        // Create another
        certificateService.findOrCreateAuthorityByName("second");
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());
    }

    @Test
    public void testFindOrCreateNodeByCertAuthorityAndCommonName() {
        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create one
        CertNode firstNode = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNode);
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        // Get the same
        CertNode firstNodeBis = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNodeBis);
        Assert.assertEquals(firstNode.getId(), firstNodeBis.getId());
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        // Create another
        CertNode secondNodeBis = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n2");
        Assert.assertNotNull(secondNodeBis);
        Assert.assertNotEquals(firstNode.getId(), secondNodeBis.getId());
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(2, certNodeRepository.count());
    }

    @Test
    public void testFindOrCreateNodeByCertAuthorityAndCommonName_LatestOne() {
        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create one
        CertNode firstNode = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNode);
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        // Get the same
        CertNode firstNodeBis = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNodeBis);
        Assert.assertEquals(firstNode.getId(), firstNodeBis.getId());
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        // Create a more recent CA
        CertAuthority certAuthority = certificateService.findOrCreateAuthorityByName("theCA").get(0);
        certAuthority.setStartDate(DateTools.addDate(new Date(), Calendar.YEAR, -1));
        certAuthority.setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, 27));
        certAuthorityRepository.save(certAuthority);
        certificateService.createFreshAuthoritiesForSoonExpiring();
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        // Should not update yet
        firstNodeBis = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNodeBis);
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());
        Assert.assertEquals(firstNode.getId(), firstNodeBis.getId());

        // Should update
        certAuthority = certificateService.findOrCreateAuthorityByName("theCA").get(0);
        certAuthority.setEndDate(DateTools.addDate(new Date(), Calendar.WEEK_OF_YEAR, 1));
        certAuthorityRepository.save(certAuthority);
        firstNodeBis = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("theCA", "n1");
        Assert.assertNotNull(firstNodeBis);
        Assert.assertNotEquals(firstNode.getCertAuthorityId(), firstNodeBis.getCertAuthorityId());
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

    }

    @Test
    public void testLifecycle() {

        Map<String, String> idMappingCa = new HashMap<>();
        Map<String, String> idMappingNode = new HashMap<>();

        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create one
        List<CertAuthority> firstCa = certificateService.findOrCreateAuthorityByName("firstCA");
        assertCerts("CertificateServiceImplTest-testLifecycle-1.txt", idMappingCa, idMappingNode);

        // Create second
        certificateService.findOrCreateAuthorityByName("secondCA");
        assertCerts("CertificateServiceImplTest-testLifecycle-2.txt", idMappingCa, idMappingNode);

        // Create some nodes
        CertNode firstCaNode1 = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first1");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first1"); // Same
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first2");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("secondCA", "second1");
        assertCerts("CertificateServiceImplTest-testLifecycle-3.txt", idMappingCa, idMappingNode);

        // Nothing soon expired
        certificateService.createFreshAuthoritiesForSoonExpiring();
        assertCerts("CertificateServiceImplTest-testLifecycle-3.txt", idMappingCa, idMappingNode);

        // Advance time for firstCA
        firstCa.get(0).setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, 27));
        certAuthorityRepository.saveAll(firstCa);
        assertCerts("CertificateServiceImplTest-testLifecycle-4.txt", idMappingCa, idMappingNode);

        // Create fresh CA (firstCA gets one)
        certificateService.createFreshAuthoritiesForSoonExpiring();
        assertCerts("CertificateServiceImplTest-testLifecycle-5.txt", idMappingCa, idMappingNode);

        certificateService.findOrCreateAuthorityByName("firstCA");
        certificateService.findOrCreateAuthorityByName("secondCA");
        assertCerts("CertificateServiceImplTest-testLifecycle-5.txt", idMappingCa, idMappingNode);

        // Create fresh (still the same)
        certificateService.createFreshAuthoritiesForSoonExpiring();
        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        assertCerts("CertificateServiceImplTest-testLifecycle-5.txt", idMappingCa, idMappingNode);

        certificateService.findOrCreateAuthorityByName("firstCA");
        certificateService.findOrCreateAuthorityByName("secondCA");
        assertCerts("CertificateServiceImplTest-testLifecycle-5.txt", idMappingCa, idMappingNode);

        // Refresh nodes (none)
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first1");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first2");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("secondCA", "second1");
        assertCerts("CertificateServiceImplTest-testLifecycle-5.txt", idMappingCa, idMappingNode);

        // Advance time for firstCA and node
        firstCa.get(0).setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, 16));
        certAuthorityRepository.saveAll(firstCa);
        firstCaNode1.setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, 16));
        certNodeRepository.save(firstCaNode1);

        // Refresh nodes (updates)
        certificateService.createFreshAuthoritiesForSoonExpiring();
        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first1");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("firstCA", "first2");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("secondCA", "second1");
        assertCerts("CertificateServiceImplTest-testLifecycle-6.txt", idMappingCa, idMappingNode);

        // Expire CA
        firstCa.get(0).setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, -5));
        certAuthorityRepository.saveAll(firstCa);
        certificateService.createFreshAuthoritiesForSoonExpiring();
        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        assertCerts("CertificateServiceImplTest-testLifecycle-7.txt", idMappingCa, idMappingNode);
    }

}
