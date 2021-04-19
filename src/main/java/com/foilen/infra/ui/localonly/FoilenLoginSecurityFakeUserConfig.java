/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.localonly;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.smalltools.hash.HashSha1;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class FoilenLoginSecurityFakeUserConfig extends AbstractBasics {

    private static final String USER_EMAIL = "admin@example.com";
    private static final String USER_ID = HashSha1.hashString(USER_EMAIL);

    @Autowired
    private UserHumanRepository userHumanRepository;

    @PostConstruct
    public void init() {
        logger.info("create Stub User");

        if (!userHumanRepository.existsById(USER_ID)) {
            userHumanRepository.save(new UserHuman(USER_ID, true).setEmail(USER_EMAIL));
        }

    }

}
