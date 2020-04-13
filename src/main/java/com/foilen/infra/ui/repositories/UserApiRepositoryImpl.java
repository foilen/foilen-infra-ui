/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.smalltools.tools.AbstractBasics;

@Service
@Transactional
public class UserApiRepositoryImpl extends AbstractBasics implements UserApiCustomRepository {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public UserApi findByUserIdAndActive(String userId, Date expireAfter) {
        Query query = new Query();

        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(new Criteria().orOperator( //
                Criteria.where("expireOn").is(null), //
                Criteria.where("expireOn").lt(expireAfter) //
        ));

        // Make the request
        return mongoOperations.findOne(query, UserApi.class);
    }

}
