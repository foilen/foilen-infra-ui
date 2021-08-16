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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.foilen.infra.ui.repositories.CertAuthorityRepository;
import com.foilen.infra.ui.repositories.CertNodeRepository;
import com.foilen.infra.ui.repositories.documents.CertAuthority;
import com.foilen.infra.ui.repositories.documents.CertNode;
import com.foilen.smalltools.crypt.spongycastle.asymmetric.RSACrypt;
import com.foilen.smalltools.crypt.spongycastle.cert.CertificateDetails;
import com.foilen.smalltools.crypt.spongycastle.cert.RSACertificate;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.StringTools;

@Service
@Transactional
public class CertificateServiceImpl extends AbstractBasics implements CertificateService {

    private static final RSACrypt rsaCrypt = new RSACrypt();
    private static final int KEY_SIZE = 4096;

    @Autowired
    private CertAuthorityRepository certAuthorityRepository;
    @Autowired
    private CertNodeRepository certNodeRepository;

    private CertAuthority createCertAuthority(String certAuthorityName) {
        RSACertificate cert = new RSACertificate(rsaCrypt.generateKeyPair(KEY_SIZE));
        cert.selfSign(new CertificateDetails() //
                .setCommonName(certAuthorityName) //
                .setEndDate(DateTools.addDate(new Date(), Calendar.MONTH, 2)) //
        );
        return new CertAuthority(certAuthorityName, //
                rsaCrypt.savePublicKeyPemAsString(cert.getKeysForSigning()), //
                rsaCrypt.savePrivateKeyPemAsString(cert.getKeysForSigning()), //
                cert.saveCertificatePemAsString(), //
                cert.getStartDate(), //
                cert.getEndDate());
    }

    @Override
    public void createFreshAuthoritiesForSoonExpiring() {
        List<String> certAuthorityNamesToRefresh = certAuthorityRepository.findAllNameByLatestEndDateBefore(DateTools.addDate(new Date(), Calendar.MONTH, 1));
        for (String certAuthorityName : certAuthorityNamesToRefresh) {
            logger.info("Creating a fresh CA for {}", certAuthorityName);
            certAuthorityRepository.save(createCertAuthority(certAuthorityName));
        }
    }

    @Override
    public List<CertAuthority> findOrCreateAuthorityByName(String certAuthorityName) {
        List<CertAuthority> certAuthorities = certAuthorityRepository.findAllByNameOrderByStartDate(certAuthorityName);
        if (certAuthorities.isEmpty()) {
            certAuthorities.add(certAuthorityRepository.save(createCertAuthority(certAuthorityName)));
        }

        return certAuthorities;
    }

    @Override
    public CertNode findOrCreateNodeByCertAuthorityAndCommonName(String certAuthorityName, String commonName) {
        Assert.notNull(certAuthorityName, "You need to specify a cert authority");

        // Get the current CA
        List<CertAuthority> certAuthorities = findOrCreateAuthorityByName(certAuthorityName);
        logger.debug("[{}] Cert authorities: {}", certAuthorityName, certAuthorities);
        CertAuthority certAuthority = certAuthorities.get(0);
        if (certAuthorities.size() > 1) {
            // The first that won't expire in 3 weeks
            for (CertAuthority toCheck : certAuthorities) {
                certAuthority = toCheck;
                logger.debug("[{}] Checking: {}", certAuthorityName, toCheck);
                if (!DateTools.isExpired(toCheck.getEndDate(), Calendar.WEEK_OF_YEAR, -3)) {
                    logger.debug("[{}] Not expiring in 3 weeks: {}", certAuthorityName, toCheck);
                    break;
                }
            }
        }
        logger.debug("[{}] Current cert authority: {}", certAuthorityName, certAuthority);

        CertNode certNode = certNodeRepository.findByCertAuthorityNameAndCommonName(certAuthorityName, commonName);
        logger.debug("[{}/{}] Current cert node: {}", certAuthorityName, commonName, certNode);
        boolean refresh = false;
        if (certNode == null) {
            logger.debug("[{}/{}] No current cert node. Create one", certAuthorityName, commonName);
            refresh = true;
        } else if (DateTools.isExpired(certNode.getEndDate(), Calendar.WEEK_OF_YEAR, -3)) {
            logger.debug("[{}/{}] Current cert node will soon expire. Refresh it", certAuthorityName, commonName);
            refresh = true;
        } else if (!StringTools.safeEquals(certAuthority.getId(), certNode.getCertAuthorityId())) {
            logger.debug("[{}/{}] Current cert node was signed with another CA. Refresh it", certAuthorityName, commonName);
            refresh = true;
        } else {
            logger.debug("[{}/{}] Current cert node is up to date: {}", certAuthorityName, commonName, certNode);
        }

        if (refresh) {

            RSACertificate caCert = RSACertificate.loadPemFromString(certAuthority.getCertificateText());
            caCert.setKeysForSigning(rsaCrypt.loadKeysPemFromString(certAuthority.getPrivateKeyText()));
            RSACertificate cert = caCert.signPublicKey(rsaCrypt.generateKeyPair(KEY_SIZE), new CertificateDetails() //
                    .setCommonName(commonName) //
                    .setEndDate(DateTools.addDate(new Date(), Calendar.MONTH, 2)) //
            );

            if (certNode == null) {
                // create
                logger.debug("[{}/{}] Create a cert node", certAuthorityName, commonName);
                certNode = certNodeRepository.save(new CertNode(certAuthority, //
                        commonName, //
                        rsaCrypt.savePublicKeyPemAsString(cert.getKeysForSigning()), //
                        rsaCrypt.savePrivateKeyPemAsString(cert.getKeysForSigning()), //
                        cert.saveCertificatePemAsString(), //
                        cert.getStartDate(), //
                        cert.getEndDate()));
            } else {
                // Update
                certNode.setCertAuthorityId(certAuthority.getId());
                certNode.setPublicKeyText(rsaCrypt.savePublicKeyPemAsString(cert.getKeysForSigning()));
                certNode.setPrivateKeyText(rsaCrypt.savePrivateKeyPemAsString(cert.getKeysForSigning()));
                certNode.setCertificateText(cert.saveCertificatePemAsString());
                certNode.setStartDate(cert.getStartDate());
                certNode.setEndDate(cert.getEndDate());
                certNode = certNodeRepository.save(certNode);
            }
        }

        return certNode;
    }

    @Override
    public void removeExpiredCertAuthoritiesAndCertNodes() {

        // CA
        List<CertAuthority> certAuthorities = certAuthorityRepository.findAllByEndDateBefore(new Date());
        for (CertAuthority certAuthority : certAuthorities) {
            certNodeRepository.deleteAllByCertAuthorityId(certAuthority.getId());
        }
        certAuthorityRepository.deleteAll(certAuthorities);

        // Nodes
        certNodeRepository.deleteAllByEndDateBefore(new Date());

    }

}
