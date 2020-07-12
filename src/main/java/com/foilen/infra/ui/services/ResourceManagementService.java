/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.model.resource.IPResource;

public interface ResourceManagementService {

    void changesExecute(ChangesContext changesContext, String defaultOwner, ResponseResourceAppliedChanges responseResourceAppliedChanges);

    /**
     * Find all the resources that match the query that the user can view.
     *
     * @param userId
     *            the user id
     * @param query
     *            the query
     * @return the resources
     * @param <T>
     *            type of resource
     */
    <T extends IPResource> List<T> resourceFindAll(String userId, IPResourceQuery<T> query);

}
