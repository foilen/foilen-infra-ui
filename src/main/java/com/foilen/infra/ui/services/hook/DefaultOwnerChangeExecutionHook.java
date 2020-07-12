/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.hook;

import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangeExecutionHook;
import com.foilen.infra.plugin.v1.core.eventhandler.changes.ChangesInTransactionContext;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.smalltools.tools.AbstractBasics;

public class DefaultOwnerChangeExecutionHook extends AbstractBasics implements ChangeExecutionHook {

    private String defaultOwnerForTx;

    public DefaultOwnerChangeExecutionHook(String defaultOwnerForTx) {
        this.defaultOwnerForTx = defaultOwnerForTx;
    }

    @Override
    public void fillApplyChangesContext(ChangesInTransactionContext changesInTransactionContext) {
        changesInTransactionContext.getVars().put(MetaConstants.META_OWNER, defaultOwnerForTx);
    }
}
