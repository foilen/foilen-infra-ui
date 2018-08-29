/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.smalltools.restapi.model.AbstractApiBaseWithError;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.tools.AbstractBasics;

public abstract class AbstractApiService extends AbstractBasics {

    protected void wrapExecution(AbstractApiBaseWithError abstractApiBaseWithError, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            ApiError error = new ApiError("Unexpected exception while executing");
            abstractApiBaseWithError.setError(error);
            logger.error("Unexpected exception while executing. Error unique id: {}", error.getUniqueId(), e);
        }
    }

}
