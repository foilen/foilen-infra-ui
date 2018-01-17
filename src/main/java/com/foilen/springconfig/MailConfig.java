/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.springconfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.foilen.smalltools.email.EmailService;
import com.foilen.smalltools.email.EmailServiceSpring;

@Configuration
public class MailConfig {

    @Value("${infraUi.mailHost}")
    private String emailHost;

    @Value("${infraUi.mailPort}")
    private int emailPort;

    @Value("${infraUi.mailUsername:#{null}}")
    private String emailUsername;

    @Value("${infraUi.mailPassword:#{null}}")
    private String emailPassword;

    @Bean
    public EmailService emailService() {
        return new EmailServiceSpring();
    }

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailHost);
        mailSender.setPort(emailPort);
        mailSender.setUsername(emailUsername);
        mailSender.setPassword(emailPassword);
        return mailSender;
    }

}
