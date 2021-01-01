/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.restapi.model.AbstractApiBaseWithError;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.CollectionsTools;
import com.google.common.base.Strings;

public abstract class AbstractApiService extends AbstractBasics {

    private static final Pattern alphaNumExtra = Pattern.compile("[A-Za-z0-9\\_\\-]*");

    @Autowired
    protected AuditingService auditingService;
    @Autowired
    protected EntitlementService entitlementService;
    @Autowired
    protected PaginationService paginationService;
    @Autowired
    protected TranslationService translationService;

    protected void addValidationError(FormResult result, String fieldName, String messageCode) {
        CollectionsTools.getOrCreateEmptyArrayList(result.getValidationErrorsByField(), fieldName, String.class).add(translationService.translate(messageCode));
    }

    protected void validateAlphaNumExtra(FormResult result, String fieldName, String fieldValue) {
        if (Strings.isNullOrEmpty(fieldValue)) {
            return;
        }

        if (!alphaNumExtra.matcher(fieldValue).matches()) {
            addValidationError(result, fieldName, "error.alphaNumExtra");
        }
    }

    protected void validateAtLeastOneManadatory(FormResult result, String[] fieldNames, String[] fieldValues) {
        for (String fieldValue : fieldValues) {
            if (!Strings.isNullOrEmpty(fieldValue)) {
                return;
            }
        }

        for (String fieldName : fieldNames) {
            addValidationError(result, fieldName, "error.atLeastOneMandatory");
        }
    }

    protected void validateManadatory(FormResult result, String fieldName, String fieldValue) {
        if (Strings.isNullOrEmpty(fieldValue)) {
            addValidationError(result, fieldName, "error.mandatory");
        }
    }

    protected void wrapExecution(AbstractApiBaseWithError abstractApiBaseWithError, Runnable runnable) {
        try {
            runnable.run();
        } catch (UiException e) {
            ApiError error = new ApiError(translationService.translate(e.getMessage()));
            abstractApiBaseWithError.setError(error);
            logger.info("UI Exception while executing. Error {}", error);
        } catch (Exception e) {
            ApiError error = new ApiError("Unexpected exception while executing");
            abstractApiBaseWithError.setError(error);
            logger.error("Unexpected exception while executing. Error unique id: {}", error.getUniqueId(), e);
        }
    }

    protected void wrapExecution(FormResult formResult, Runnable runnable) {
        try {
            runnable.run();
        } catch (UiException e) {
            formResult.getGlobalErrors().add(translationService.translate(e.getMessage()));
        } catch (Exception e) {
            ApiError error = new ApiError("Unexpected exception while executing");
            formResult.setError(error);
            logger.error("Unexpected exception while executing. Error unique id: {}", error.getUniqueId(), e);
        }
    }

}
