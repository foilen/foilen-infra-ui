/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.services;

import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.db.domain.audit.AuditUserType;

public interface AuditingService {

    void linkAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource fromResource, String linkType, IPResource toResource //
    );

    void linkDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource fromResource, String linkType, IPResource toResource //
    );

    void resourceAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource //
    );

    void resourceDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource //
    );

    void resourceUpdate(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource beforeResource, IPResource afterResource //
    );

    void tagAdd(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource, String tagName //
    );

    void tagDelete(String txId, boolean explicitChange, //
            AuditUserType userType, String userName, //
            IPResource resource, String tagName //
    );

}
