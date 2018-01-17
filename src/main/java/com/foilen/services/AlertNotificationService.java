/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.services;

/**
 * Send emails for alerts.
 */
public interface AlertNotificationService {

    /**
     * Send an alert.
     *
     * @param subject
     *            the subject
     * @param content
     *            the content
     */
    void sendAlert(String subject, String content);

}
