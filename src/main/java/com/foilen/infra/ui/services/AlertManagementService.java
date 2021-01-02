/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.smalltools.restapi.model.FormResult;

public interface AlertManagementService {

    /**
     * Queue up an alert to send as the SYSTEM.
     *
     * @param subject
     *            the subject
     * @param content
     *            the content
     */
    void queueAlert(String subject, String content);

    /**
     * Queue up an alert to send.
     *
     * @param userId
     *            the user sending the alert
     * @param subject
     *            the subject
     * @param content
     *            the content
     * @return the result
     */
    FormResult queueAlert(String userId, String subject, String content);

    /**
     * Send all the queued alerts that are queued if any is at least 30 seconds old.
     */
    void sendQueuedAlerts();

}
