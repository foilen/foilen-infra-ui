/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import java.util.Arrays;

import org.bson.BsonNull;
import org.springframework.stereotype.Component;

import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

@Component
public class V2020041901_Ui_CollectionsAndIndexes_UserPermissions extends AbstractMongoUpgradeTask {

    @Override
    public void execute() {
        addCollection("ownerRule");
        addCollection("role");

        addView("allOwnersDefinedInRoles", "role", Arrays.asList( //
                Aggregates.project( //
                        Projections.fields( //
                                Projections.excludeId(), //
                                Projections.computed("owners", Filters.eq("$concatArrays", Arrays.asList("$resources.owner", "$links.fromOwner", "$links.toOwner"))) //
                        )), //
                Aggregates.unwind("$owners"), //
                Aggregates.group("$owners"), //
                Aggregates.sort(Sorts.ascending("_id")), //
                Aggregates.match(Filters.nin("_id", Arrays.asList("*", "", new BsonNull()))) //
        ));

    }

}
