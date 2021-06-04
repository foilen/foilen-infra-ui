/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.ui.repositories.CertAuthorityRepository;
import com.foilen.infra.ui.repositories.CertNodeRepository;
import com.foilen.infra.ui.repositories.documents.CertAuthority;
import com.foilen.infra.ui.repositories.documents.CertNode;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.tools.DateTools;

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

    @Test
    public void testCreateFreshAuthoritiesForSoonExpiring() {
        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create one
        List<CertAuthority> firstCa = certificateService.findOrCreateAuthorityByName("firstCA");
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create second
        certificateService.findOrCreateAuthorityByName("secondCA");
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Nothing soon expired
        certificateService.createFreshAuthoritiesForSoonExpiring();
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Advance time
        firstCa.get(0).setEndDate(DateTools.addDate(new Date(), Calendar.WEEK_OF_YEAR, 3));
        certAuthorityRepository.saveAll(firstCa);

        certificateService.createFreshAuthoritiesForSoonExpiring();
        Assert.assertEquals(3, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        List<CertAuthority> firstCaBis = certificateService.findOrCreateAuthorityByName("firstCA");
        List<CertAuthority> secondCaBis = certificateService.findOrCreateAuthorityByName("secondCA");
        Assert.assertEquals(2, firstCaBis.size());
        Assert.assertEquals(1, secondCaBis.size());
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
    public void testRemoveExpiredCertAuthoritiesAndCertNodes() {

        Assert.assertEquals(0, certAuthorityRepository.count());
        Assert.assertEquals(0, certNodeRepository.count());

        // Create CA and nodes
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("expiredCA", "n1");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("expiredCA", "n2");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("goodCA", "n1-expired");
        certificateService.findOrCreateNodeByCertAuthorityAndCommonName("goodCA", "n2-good");

        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(4, certNodeRepository.count());

        // Nothing changes
        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        Assert.assertEquals(2, certAuthorityRepository.count());
        Assert.assertEquals(4, certNodeRepository.count());

        // Expire
        CertAuthority expiredCa = certificateService.findOrCreateAuthorityByName("expiredCA").get(0);
        expiredCa.setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, -1));
        certAuthorityRepository.save(expiredCa);

        CertNode expiredNode = certificateService.findOrCreateNodeByCertAuthorityAndCommonName("goodCA", "n1-expired");
        expiredNode.setEndDate(DateTools.addDate(new Date(), Calendar.DAY_OF_YEAR, -1));
        certNodeRepository.save(expiredNode);

        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        Assert.assertEquals(1, certAuthorityRepository.count());
        Assert.assertEquals(1, certNodeRepository.count());

        Assert.assertEquals("goodCA", certAuthorityRepository.findAll().get(0).getName());
        Assert.assertEquals("n2-good", certNodeRepository.findAll().get(0).getCommonName());
    }

}
