/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.apitmp.model;

import com.foilen.smalltools.restapi.model.AbstractSingleResult;

public class ApplicationDetailsResult extends AbstractSingleResult<ApplicationDetails> {

    public ApplicationDetailsResult() {
    }

    public ApplicationDetailsResult(ApplicationDetails item) {
        this.setItem(item);
    }

}
