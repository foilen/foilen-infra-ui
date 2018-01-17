/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.plugin.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;

@Component
public class InternalServicesContextBean extends InternalServicesContext {

    @Autowired
    private InternalIPResourceService internalIPResourceService;

    @Autowired
    private InternalChangeService internalChangeService;

    public InternalServicesContextBean() {
        super(null, null);
    }

    @Override
    public InternalChangeService getInternalChangeService() {
        return internalChangeService;
    }

    @Override
    public InternalIPResourceService getInternalIPResourceService() {
        return internalIPResourceService;
    }

}
