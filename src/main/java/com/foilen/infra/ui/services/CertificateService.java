/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.ui.repositories.documents.CertAuthority;
import com.foilen.infra.ui.repositories.documents.CertNode;

public interface CertificateService {

    /**
     * Search for CA that expire in less than 1 month. Create a fresh cert.
     */
    void createFreshAuthoritiesForSoonExpiring();

    /**
     * Get at least one authority. There can be overlap during the transition to a more recent cert.
     *
     * @param certAuthorityName
     *            the name of the cert authority
     * @return all the cert authorities in start order
     */
    List<CertAuthority> findOrCreateAuthorityByName(String certAuthorityName);

    /**
     * Get or create the cert for the specified authority name. It will use the current authority.
     *
     * @param certAuthorityName
     *            the name of the cert authority
     * @param commonName
     *            the common name of the node
     * @return the cert
     */
    CertNode findOrCreateNodeByCertAuthorityAndCommonName(String certAuthorityName, String commonName);

    void removeExpiredCertAuthoritiesAndCertNodes();

}
