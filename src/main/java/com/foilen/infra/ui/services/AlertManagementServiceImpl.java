/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.ui.db.dao.AlertToSendDao;
import com.foilen.infra.ui.db.dao.ApiMachineUserDao;
import com.foilen.infra.ui.db.domain.alert.AlertToSend;
import com.foilen.smalltools.email.EmailService;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;

@Service
@Transactional
public class AlertManagementServiceImpl extends AbstractBasics implements AlertManagementService {

    @Autowired
    private AlertToSendDao alertToSendDao;
    @Autowired
    private ApiMachineUserDao apiMachineUserDao;
    @Autowired
    private EntitlementService entitlementService;

    @Autowired
    private EmailService emailService;

    @Value("${infraUi.mailFrom}")
    private String emailFrom;

    @Value("${infraUi.mailAlertsTo}")
    private String emailTo;

    @Override
    public void queueAlert(String subject, String content) {
        alertToSendDao.save(new AlertToSend(new Date(), "UI", subject, content));
    }

    @Override
    public FormResult queueAlert(String userId, String subject, String content) {

        FormResult formResult = new FormResult();

        if (!entitlementService.canSendAlert(userId)) {
            formResult.getGlobalErrors().add("You are not allowed");
            return formResult;
        }

        String machineName = apiMachineUserDao.findByUserId(userId).getMachineName();
        alertToSendDao.save(new AlertToSend(new Date(), machineName, subject, content));

        return formResult;
    }

    @Override
    public void sendQueuedAlerts() {

        if (alertToSendDao.countBySentOnBefore(DateTools.addDate(Calendar.SECOND, -30)) == 0) {
            return;
        }

        List<AlertToSend> alertsToSend = alertToSendDao.findAll(Sort.by("sender", "subject", "sentOn"));

        // Choose the subject
        String subject;
        if (alertsToSend.size() == 1) {
            subject = alertsToSend.get(0).getSender();
        } else {
            subject = "Multiple alerts";
        }

        // Fill the content
        StringBuilder content = new StringBuilder();
        alertsToSend.forEach(it -> {
            content.append(it.getSender()) //
                    .append(" | ").append(it.getSubject()) //
                    .append(" | ").append(DateTools.formatFull(it.getSentOn())) //
                    .append(" | ").append(it.getContent()) //
                    .append("\n");
        });

        // Send
        try {
            emailService.sendTextEmail(emailFrom, emailTo, subject, content.toString());
        } catch (Exception e) {
            logger.error("Cannot send email", e);
            return;
        }

        // Delete
        alertToSendDao.deleteAll(alertsToSend);

    }

}
