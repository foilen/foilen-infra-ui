/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.apitmp.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;

public class AuditItemWithPagination extends AbstractListResultWithPagination<AuditItem> {

    private Map<String, List<String>> validationErrorsByField = new HashMap<>();

    public Map<String, List<String>> getValidationErrorsByField() {
        return validationErrorsByField;
    }

    @Override
    public boolean isSuccess() {
        return validationErrorsByField.isEmpty() && super.isSuccess();
    }

    public AuditItemWithPagination setValidationErrorsByField(Map<String, List<String>> validationErrorsByField) {
        this.validationErrorsByField = validationErrorsByField;
        return this;
    }

}
