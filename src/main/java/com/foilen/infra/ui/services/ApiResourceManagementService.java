/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import com.foilen.infra.api.request.RequestChanges;
import com.foilen.infra.api.request.RequestResourceSearch;
import com.foilen.infra.api.response.ResponseResourceAppliedChanges;
import com.foilen.infra.api.response.ResponseResourceBucket;
import com.foilen.infra.api.response.ResponseResourceBuckets;
import com.foilen.infra.api.response.ResponseResourceTypesDetails;

public interface ApiResourceManagementService {

    /**
     * Apply the changes as the currently logged in user.
     *
     * @param userId
     *            the user id
     * @param changes
     *            the changes to make
     * @return the result
     */
    ResponseResourceAppliedChanges applyChanges(String userId, RequestChanges changes);

    ResponseResourceBuckets resourceFindAll(String userId, RequestResourceSearch resourceSearch);

    ResponseResourceBucket resourceFindOne(String userId, RequestResourceSearch resourceSearch);

    ResponseResourceTypesDetails resourceTypeFindAll();

}
