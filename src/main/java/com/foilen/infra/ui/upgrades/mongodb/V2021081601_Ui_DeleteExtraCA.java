/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.repositories.CertAuthorityRepository;
import com.foilen.infra.ui.repositories.CertNodeRepository;
import com.foilen.infra.ui.repositories.documents.CertNode;

@Component
public class V2021081601_Ui_DeleteExtraCA extends AbstractMongoUpgradeTask {

    @Autowired
    private CertAuthorityRepository certAuthorityRepository;
    @Autowired
    private CertNodeRepository certNodeRepository;

    @Override
    public void execute() {

        logger.info("Check all the nodes certs");
        List<String> usedCertAuthIds = certNodeRepository.findAll().stream().map(CertNode::getCertAuthorityId).sorted().distinct().collect(Collectors.toList());
        logger.info("Got {} different ids", usedCertAuthIds.size());

        logger.info("Delete all unused CAs");
        certAuthorityRepository.deleteAllByIdNotIn(usedCertAuthIds);

    }

}
