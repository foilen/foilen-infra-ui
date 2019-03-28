/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.domain.alert;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class AlertToSend implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Date sentOn;
    private String sender;

    private String subject;
    private String content;

    public AlertToSend() {
    }

    public AlertToSend(Date sentOn, String sender, String subject, String content) {
        this.sentOn = sentOn;
        this.sender = sender;
        this.subject = subject;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public Long getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public Date getSentOn() {
        return sentOn;
    }

    public String getSubject() {
        return subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setSentOn(Date sentOn) {
        this.sentOn = sentOn;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

}
