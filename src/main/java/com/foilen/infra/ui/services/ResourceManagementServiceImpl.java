/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.ui.services.hook.FillResponseChangeExecutionHook;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional
public class ResourceManagementServiceImpl extends AbstractBasics implements ResourceManagementService {

    @Autowired
    private AuditingService auditingService;
    @Autowired
    private InternalServicesContext internalServicesContext;

    @Override
    public void changesExecute(ChangesContext changes, ResponseResourceAppliedChanges responseResourceAppliedChanges) {
        internalServicesContext.getInternalChangeService().changesExecute(changes, Collections.singletonList(new FillResponseChangeExecutionHook(auditingService, responseResourceAppliedChanges)));
    }

}
