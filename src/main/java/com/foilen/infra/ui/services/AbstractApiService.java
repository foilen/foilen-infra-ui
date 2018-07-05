/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.api.response.ResponseWithStatus;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.SecureRandomTools;

public abstract class AbstractApiService extends AbstractBasics {

    private String baseId = SecureRandomTools.randomHexString(5) + "/";

    protected void wrapExecution(ResponseWithStatus response, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            String errorId = baseId + SecureRandomTools.randomHexString(5);
            logger.error("Unexpected exception while executing. Error unique id: {}", errorId, e);
            response.addError("Unexpected exception while executing. Error unique id: " + baseId);
        }
    }

}
