/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test.mock;

import com.foilen.smalltools.email.EmailBuilder;
import com.foilen.smalltools.email.EmailService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;

public class EmailServiceMock extends AbstractBasics implements EmailService {

    @Override
    public void sendEmail(EmailBuilder emailBuilder) {
        logger.info("Sending email: {}", JsonTools.prettyPrint(emailBuilder));
    }

    @Override
    public void sendHtmlEmail(String from, String to, String subject, String html) {
        logger.info("Sending email: {} -> {} | {} | {}", from, to, subject, html);
    }

    @Override
    public void sendTextEmail(String from, String to, String subject, String text) {
        logger.info("Sending email: {} -> {} | {} | {}", from, to, subject, text);
    }

}
