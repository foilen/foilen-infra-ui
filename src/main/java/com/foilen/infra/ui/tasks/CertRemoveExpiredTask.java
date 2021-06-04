/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.services.CertificateService;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class CertRemoveExpiredTask extends AbstractBasics {

    @Autowired
    private CertificateService certificateService;

    // Every 1 day
    @Scheduled(cron = "0 45 */2 * * *")
    public void removeExpiredCerts() {
        logger.info("Starting removing expired certs");
        certificateService.removeExpiredCertAuthoritiesAndCertNodes();
        logger.info("Completed removing expired certs");
    }

}
