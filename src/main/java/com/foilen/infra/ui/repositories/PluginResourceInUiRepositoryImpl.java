/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional
public class PluginResourceInUiRepositoryImpl extends AbstractBasics implements PluginResourceInUiCustomRepository {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public List<PluginResource> findAllWithoutOwner() {
        return mongoOperations.find(new Query(new Criteria().orOperator( //
                new Criteria("resource.meta." + MetaConstants.META_OWNER).exists(false), //
                new Criteria("resource.meta." + MetaConstants.META_OWNER).is(null) //
        ) //
        ), //
                PluginResource.class);
    }

    @Override
    public void updateOwner(String internalId, String owner) {
        mongoOperations.updateFirst( //
                new Query().addCriteria(Criteria.where("id").is(internalId)), //
                new Update().set("resource.meta." + MetaConstants.META_OWNER, owner), //
                PluginResource.class);
    }

}
