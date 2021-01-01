/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test;

import org.junit.Assert;

import com.foilen.login.spring.client.security.FoilenLoginUserDetails;
import com.foilen.login.spring.services.FoilenLoginService;

public class FoilenLoginServiceMock implements FoilenLoginService {

    @Override
    public FoilenLoginUserDetails getLoggedInUserDetails() {
        Assert.fail("Not supposed to be used");
        return null;
    }

}
