/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.api.model.ui.ApplicationDetailsResult;

public interface ApplicationService {

    ApplicationDetailsResult getDetails(String userId);

}
