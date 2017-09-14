/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import com.foilen.infra.api.request.ChangesRequest;
import com.foilen.infra.api.response.ResponseWithStatus;

public interface ApiResourceManagementService {

    /**
     * Apply the changes as the currently logged in user.
     *
     * @param changesRequest
     *            the changes to make
     * @return the result
     */
    ResponseWithStatus applyChanges(ChangesRequest changesRequest);

}
