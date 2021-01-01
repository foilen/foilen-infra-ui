/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.infra.ui.upgrades.mongodb.tmp.TmpTranslationServiceImpl;
import com.foilen.smalltools.restapi.model.FormResult;

public class AbstractApiServiceTest {

    public class TestAbstractApiService extends AbstractApiService {

    }

    private TestAbstractApiService service = new TestAbstractApiService();

    public AbstractApiServiceTest() {
        service.translationService = new TmpTranslationServiceImpl();
    }

    private void testValidateAlphaNumExtra(boolean expected, String fieldValue) {
        FormResult result = new FormResult();
        service.validateAlphaNumExtra(result, "ok", fieldValue);
        Assert.assertEquals(expected, result.isSuccess());
    }

    @Test
    public void testValidateAlphaNumExtra_FailDot() {
        testValidateAlphaNumExtra(false, "ABCab.c");
    }

    @Test
    public void testValidateAlphaNumExtra_FailPlus() {
        testValidateAlphaNumExtra(false, "ABCab+fg");
    }

    @Test
    public void testValidateAlphaNumExtra_FailSlash() {
        testValidateAlphaNumExtra(false, "ABCab/c");
    }

    @Test
    public void testValidateAlphaNumExtra_OK_All() {
        testValidateAlphaNumExtra(true, "ABCabc123_-Z");
    }

    @Test
    public void testValidateAlphaNumExtra_OK_Alpha() {
        testValidateAlphaNumExtra(true, "abc");
    }

    @Test
    public void testValidateAlphaNumExtra_OK_Empty() {
        testValidateAlphaNumExtra(true, "");
    }

    @Test
    public void testValidateAlphaNumExtra_OK_Null() {
        testValidateAlphaNumExtra(true, null);
    }

    @Test
    public void testValidateAlphaNumExtra_OK_Num() {
        testValidateAlphaNumExtra(true, "123");
    }

}
