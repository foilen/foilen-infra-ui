/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.foilen.smalltools.email.EmailService;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
public class AlertNotificationServiceImpl extends AbstractBasics implements AlertNotificationService {

    @Autowired
    private EmailService emailService;

    @Value("${infraUi.mailFrom}")
    private String emailFrom;

    @Value("${infraUi.mailAlertsTo}")
    private String emailTo;

    @Override
    public void sendAlert(String subject, String content) {
        try {
            emailService.sendTextEmail(emailFrom, emailTo, subject, content);
        } catch (Exception e) {
            logger.error("Cannot send email", e);
        }
    }

}
