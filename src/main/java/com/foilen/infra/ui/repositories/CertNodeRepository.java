/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.repositories.documents.CertNode;

@Service
public interface CertNodeRepository extends MongoRepository<CertNode, String> {

    void deleteAllByCertAuthorityId(String certAuthorityName);

    void deleteAllByEndDateBefore(Date endDate);

    CertNode findByCertAuthorityNameAndCommonName(String certAuthorityName, String commonName);

}
