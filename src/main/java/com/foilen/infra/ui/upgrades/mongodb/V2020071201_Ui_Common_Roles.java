/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.upgrades.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.foilen.infra.api.model.permission.LinkAction;
import com.foilen.infra.api.model.permission.PermissionLink;
import com.foilen.infra.api.model.permission.PermissionResource;
import com.foilen.infra.api.model.permission.ResourceAction;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.MetaConstants;
import com.foilen.infra.ui.repositories.OwnerRuleRepository;
import com.foilen.infra.ui.repositories.RoleRepository;
import com.foilen.infra.ui.repositories.documents.OwnerRule;
import com.foilen.infra.ui.repositories.documents.Role;

@Component
public class V2020071201_Ui_Common_Roles extends AbstractMongoUpgradeTask {

    private static final String INFRA = "infra";
    private static final String SHARED_MACHINE = "shared_machine";

    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private OwnerRuleRepository ownerRuleRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void execute() {

        logger.info("OwnerRule for infra_");
        ownerRuleRepository.save(new OwnerRule().setResourceNameStartsWith("infra_").setAssignOwner("infra"));
        mongoOperations.updateMulti( //
                new Query().addCriteria(Criteria.where("resource.name").regex("^infra_.*")), //
                new Update().set("resource.meta." + MetaConstants.META_OWNER, "infra"), //
                PluginResource.class);

        logger.info("Role {}", INFRA);
        Role role = new Role(INFRA);
        role.getResources().add(new PermissionResource() //
                .setAction(ResourceAction.LIST) //
                .setExplicitChange(false) //
                .setType("*") //
                .setOwner(INFRA) //
        );
        role.getLinks().add(new PermissionLink() //
                .setAction(LinkAction.ALL) //
                .setExplicitChange(false) //
                .setFromOwner(INFRA) //
                .setFromType("*") //
                .setLinkType("*") //
                .setToOwner(null) //
                .setToType(null) //
        );
        role.getLinks().add(new PermissionLink() //
                .setAction(LinkAction.ALL) //
                .setExplicitChange(false) //
                .setFromOwner(null) //
                .setFromType(null) //
                .setLinkType("*") //
                .setToOwner(INFRA) //
                .setToType("*") //
        );
        roleRepository.save(role);

        logger.info("Role {}", SHARED_MACHINE);
        role = new Role(SHARED_MACHINE);
        role.getResources().add(new PermissionResource() //
                .setAction(ResourceAction.LIST) //
                .setExplicitChange(false) //
                .setType(Machine.RESOURCE_TYPE) //
                .setOwner(SHARED_MACHINE) //
        );
        role.getLinks().add(new PermissionLink() //
                .setAction(LinkAction.ALL) //
                .setExplicitChange(true) //
                .setFromOwner(null) //
                .setFromType(null) //
                .setLinkType(LinkTypeConstants.INSTALLED_ON) //
                .setToOwner(SHARED_MACHINE) //
                .setToType(Machine.RESOURCE_TYPE) //
        );
        role.getLinks().add(new PermissionLink() //
                .setAction(LinkAction.ALL) //
                .setExplicitChange(false) //
                .setFromOwner(SHARED_MACHINE) //
                .setFromType(Machine.RESOURCE_TYPE) //
                .setLinkType("*") //
                .setToOwner(null) //
                .setToType(null) //
        );
        role.getLinks().add(new PermissionLink() //
                .setAction(LinkAction.ALL) //
                .setExplicitChange(false) //
                .setFromOwner(null) //
                .setFromType(null) //
                .setLinkType("*") //
                .setToOwner(SHARED_MACHINE) //
                .setToType(Machine.RESOURCE_TYPE) //
        );
        roleRepository.save(role);

    }

}
