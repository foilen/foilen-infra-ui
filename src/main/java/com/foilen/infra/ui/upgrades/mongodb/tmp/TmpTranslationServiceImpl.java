/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb.tmp;

import java.util.Locale;

import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.smalltools.tools.AbstractBasics;

public class TmpTranslationServiceImpl extends AbstractBasics implements TranslationService {

    @Override
    public String translate(Locale locale, String messageCode) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public String translate(Locale locale, String messageCode, Object... args) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public String translate(String messageCode) {
        return messageCode;
    }

    @Override
    public String translate(String messageCode, Object... args) {
        throw new RuntimeException("Not mocked");
    }

    @Override
    public void translationAdd(String basename) {
    }

}
