/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.testonly;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.smalltools.tools.AbstractBasics;

@Component
public class FoilenLoginSecurityFakeUserConfig extends AbstractBasics {

    private static final String USER_ID = "111111";
    @Autowired
    private UserHumanRepository userHumanRepository;

    @PostConstruct
    public void init() {
        logger.info("create Stub User");

        if (!userHumanRepository.existsById(USER_ID)) {
            userHumanRepository.save(new UserHuman(USER_ID, true).setEmail("fake@example.com"));
        }

    }

}
