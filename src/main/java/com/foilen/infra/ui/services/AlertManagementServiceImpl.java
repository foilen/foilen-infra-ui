/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.plugin.core.system.mongodb.repositories.MessageRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.Message;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.models.MessageLevel;
import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.smalltools.email.EmailService;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.google.common.collect.ComparisonChain;

@Service
@Transactional
public class AlertManagementServiceImpl extends AbstractBasics implements AlertManagementService {

    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;

    @Autowired
    private EmailService emailService;

    @Value("${infraUi.mailFrom}")
    private String emailFrom;

    @Value("${infraUi.mailAlertsTo}")
    private String emailTo;

    @Override
    public void queueAlert(String subject, String content) {
        messageRepository.save(new Message(MessageLevel.INFO, new Date(), "UI", subject, content));
    }

    @Override
    public FormResult queueAlert(String userId, String subject, String content) {

        FormResult formResult = new FormResult();

        if (!entitlementService.canSendAlert(userId)) {
            formResult.getGlobalErrors().add("You are not allowed");
            return formResult;
        }

        // Change the sender's name for the machine name if it is one
        String sender = userId;
        Optional<UserApiMachine> optionalUserApiMachine = userApiMachineRepository.findById(userId);
        if (optionalUserApiMachine.isPresent()) {
            sender = optionalUserApiMachine.get().getMachineName();
        }
        messageRepository.save(new Message(MessageLevel.INFO, new Date(), sender, subject, content));

        return formResult;
    }

    @Override
    public void sendQueuedAlerts() {

        if (messageRepository.countBySentOnBeforeAndAcknowledgedIsFalse(DateTools.addDate(Calendar.SECOND, -30)) == 0) {
            return;
        }

        List<Message> alertsToSend = messageRepository.findAllNotAcknowledgedAndAcknowledgedThem();
        Collections.sort(alertsToSend, (a, b) -> ComparisonChain.start() //
                .compare(a.getSender(), b.getSender()) //
                .compare(a.getShortDescription(), b.getShortDescription()) //
                .compare(a.getSentOn(), b.getSentOn()) //
                .result());

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
                    .append(" | ").append(it.getShortDescription()) //
                    .append(" | ").append(DateTools.formatFull(it.getSentOn())) //
                    .append(" | ").append(it.getLongDescription()) //
                    .append("\n");
        });

        // Send
        try {
            emailService.sendTextEmail(emailFrom, emailTo, subject, content.toString());
        } catch (Exception e) {
            logger.error("Cannot send email", e);
            return;
        }

        // Delete alerts from 1 week ago
        messageRepository.deleteBySentOnBeforeAndAcknowledgedIsTrue(DateTools.addDate(Calendar.WEEK_OF_YEAR, -1));

    }

}
