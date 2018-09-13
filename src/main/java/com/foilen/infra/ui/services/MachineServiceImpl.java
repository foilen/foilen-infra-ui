/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.MachineSetup;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.ui.db.domain.user.ApiMachineUser;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.StringTools;

@Service
@Transactional
public class MachineServiceImpl extends AbstractBasics implements MachineService {

    @Autowired
    private ApiUserService apiUserService;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private EntitlementService entitlementService;

    @Value("${infraUi.baseUrl}")
    private String uiApiBaseUrl;
    @Value("${infraUi.certText:#{null}}")
    private String uiApiCert;

    @Override
    public MachineSetup getMachineSetup(String machineName) {

        // Get the machine if present
        Optional<Machine> machineOptional = ipResourceService.resourceFind(ipResourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, machineName));
        if (!machineOptional.isPresent()) {
            return null;
        }
        Machine machine = machineOptional.get();

        MachineSetup machineSetup = new MachineSetup();
        machineSetup.setMachineName(machineName);

        // Retrieve API access details
        ApiMachineUser apiUser = apiUserService.getOrCreateForMachine(machineName);
        machineSetup.setUiApiBaseUrl(uiApiBaseUrl);
        machineSetup.setUiApiCert(uiApiCert);
        machineSetup.setUiApiUserId(apiUser.getUserId());
        machineSetup.setUiApiUserKey(apiUser.getUserKey());

        // Retrieve what is installed on this machine
        List<Application> applications = ipResourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Application.class, LinkTypeConstants.INSTALLED_ON, machine);
        machineSetup.setApplications(applications);
        List<UnixUser> unixUsers = ipResourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(UnixUser.class, LinkTypeConstants.INSTALLED_ON, machine);
        machineSetup.setUnixUsers(unixUsers);

        // Add any missing users that are used by the applications
        Set<UnixUser> additionnalUnixUsers = new HashSet<>();
        for (Application application : applications) {
            additionnalUnixUsers.addAll(ipResourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.RUN_AS, UnixUser.class));
        }
        additionnalUnixUsers.removeAll(unixUsers);

        // Add unix users that must be on all machines
        additionnalUnixUsers.addAll(ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, "infra_docker_manager")));// TODO Do not hardcode the docker manager unix user name

        unixUsers.addAll(additionnalUnixUsers);

        // Remove all system unix users
        unixUsers.removeIf(it -> it instanceof SystemUnixUser);

        Collections.sort(applications);
        Collections.sort(unixUsers, (a, b) -> Long.compare(a.getId(), b.getId()));

        return machineSetup;
    }

    @Override
    public List<String> list(String userId) {
        if (entitlementService.canManageAllMachines(userId)) {
            return ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class)).stream() //
                    .map(it -> it.getName()) //
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Machine> listMachines(String userId) {
        if (entitlementService.canManageAllMachines(userId)) {
            return ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> listMonitor(String userId) {
        if (entitlementService.canManageAllMachines(userId)) {
            return ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class)).stream() //
                    .map(it -> it.getName()) //
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }

    }

    @Override
    public void updateIpIfAvailable(String userId, String machineName, String ipPublic) {

        if (!entitlementService.canManageMachine(userId, machineName)) {
            throw new AccessDeniedException("Cannot manage the machine");
        }

        // Get the machine if present
        Optional<Machine> machineOptional = ipResourceService.resourceFind(ipResourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, machineName));
        if (!machineOptional.isPresent()) {
            return;
        }

        // Change the ip if different
        Machine machine = machineOptional.get();
        if (!StringTools.safeEquals(ipPublic, machine.getPublicIp())) {
            logger.info("Updating machine {} IP to {}", machineName, ipPublic);
            ChangesContext changes = new ChangesContext(ipResourceService);
            machine.setPublicIp(ipPublic);
            changes.resourceUpdate(machine.getInternalId(), machine);
            internalChangeService.changesExecute(changes);
        }
    }

}
