/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.machine.MachineSetup;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.cronjob.CronJob;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;

@Service
@Transactional
public class MachineServiceImpl extends AbstractBasics implements MachineService {

    @Autowired
    private UserApiService userApiService;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private EntitlementService entitlementService;
    @Autowired
    private ResourceManagementService resourceManagementService;

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
        UserApiMachine apiUser = userApiService.getOrCreateForMachine(machineName);
        machineSetup.setUiApiBaseUrl(uiApiBaseUrl);
        machineSetup.setUiApiCert(uiApiCert);
        machineSetup.setUiApiUserId(apiUser.getUserId());
        machineSetup.setUiApiUserKey(apiUser.getUserKey());

        // Retrieve what is installed on this machine
        List<Application> applications = ipResourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(Application.class, LinkTypeConstants.INSTALLED_ON, machine);
        machineSetup.setApplications(applications.stream().map(it -> JsonTools.clone(it, com.foilen.infra.api.model.machine.Application.class)).collect(Collectors.toList()));
        // Retrieve cron jobs
        List<CronJob> cronJobs = ipResourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(CronJob.class, LinkTypeConstants.INSTALLED_ON, machine);
        Set<UnixUser> cronJobsUnixUsers = new HashSet<>();
        machineSetup.setCronJobs(cronJobs.stream() //
                .map(it -> {
                    com.foilen.infra.api.model.machine.CronJob apiCronJob = JsonTools.clone(it, com.foilen.infra.api.model.machine.CronJob.class);
                    List<Application> cronApps = ipResourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(it, LinkTypeConstants.USES, Application.class);
                    if (!cronApps.isEmpty()) {
                        apiCronJob.setApplicationName(cronApps.get(0).getName());
                    }
                    List<UnixUser> cronUnixUsers = ipResourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(it, LinkTypeConstants.RUN_AS, UnixUser.class);
                    if (!cronUnixUsers.isEmpty()) {
                        UnixUser cronUnixUser = cronUnixUsers.get(0);
                        apiCronJob.setRunAs(JsonTools.clone(cronUnixUser, com.foilen.infra.api.model.machine.UnixUser.class));
                        cronJobsUnixUsers.add(cronUnixUser);
                    }
                    return apiCronJob;
                }) //
                .filter(cron -> cron.getRunAs() != null) //
                .filter(cron -> cron.getApplicationName() != null) //
                .filter(cron -> applications.stream().anyMatch(app -> app.getName().equals(cron.getApplicationName()))) //
                .collect(Collectors.toList()));

        List<UnixUser> unixUsers = ipResourceService.linkFindAllByFromResourceClassAndLinkTypeAndToResource(UnixUser.class, LinkTypeConstants.INSTALLED_ON, machine);

        // Add any missing users that are used by the applications and cron jobs
        Set<UnixUser> additionnalUnixUsers = new HashSet<>();
        for (Application application : applications) {
            additionnalUnixUsers.addAll(ipResourceService.linkFindAllByFromResourceAndLinkTypeAndToResourceClass(application, LinkTypeConstants.RUN_AS, UnixUser.class));
        }
        additionnalUnixUsers.addAll(cronJobsUnixUsers);
        additionnalUnixUsers.removeAll(unixUsers);

        // Add unix users that must be on all machines
        additionnalUnixUsers.addAll(ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(UnixUser.class) //
                .propertyEquals(UnixUser.PROPERTY_NAME, "infra_docker_manager")));// TODO Do not hardcode the docker manager unix user name

        unixUsers.addAll(additionnalUnixUsers);

        // Remove all system unix users
        unixUsers.removeIf(it -> it instanceof SystemUnixUser);

        Collections.sort(applications);
        Collections.sort(unixUsers, (a, b) -> Long.compare(a.getId(), b.getId()));

        machineSetup.setUnixUsers(unixUsers.stream().map(it -> JsonTools.clone(it, com.foilen.infra.api.model.machine.UnixUser.class)).collect(Collectors.toList()));

        return machineSetup;
    }

    @Override
    public List<Machine> listMachines(String userId) {
        return resourceManagementService.resourceFindAll(userId, ipResourceService.createResourceQuery(Machine.class));
    }

    @Override
    public List<String> listMonitor(String userId) {
        return resourceManagementService.resourceFindAll(userId, ipResourceService.createResourceQuery(Machine.class)).stream() //
                .map(it -> it.getName()) //
                .collect(Collectors.toList());
    }

    @Override
    public void updateIpIfAvailable(String userId, String machineName, String ipPublic) {

        logger.debug("updateIpIfAvailable: userId: {} ; machineName: {} ; ipPublic: {} ; ", userId, machineName, ipPublic);

        if (!entitlementService.canManageMachine(userId, machineName)) {
            throw new AccessDeniedException("Cannot manage the machine");
        }

        // Get the machine if present
        Optional<Machine> machineOptional = ipResourceService.resourceFind(ipResourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, machineName));
        if (!machineOptional.isPresent()) {
            logger.debug("The machine {} does not exist", machineName);
            return;
        }

        // Change the ip if different
        Machine machine = machineOptional.get();
        String machineIp = machine.getPublicIp();
        logger.debug("The machine {} has IP {}", machineName, machineIp);
        if (StringTools.safeEquals(ipPublic, machineIp)) {
            logger.debug("The IP is the same");
        } else {
            logger.info("Updating machine {} IP to {}", machineName, ipPublic);

            // Make the change as the SYSTEM if it is the machine itself
            SecurityContext oldContext = null;
            if (entitlementService.isTheMachine(userId, machineName)) {
                logger.info("The user is the machine, switching to SYSTEM as the user");
                oldContext = SecurityContextHolder.getContext();
                SecurityContextHolder.clearContext();
            }

            try {
                ChangesContext changes = new ChangesContext(ipResourceService);
                machine.setPublicIp(ipPublic);
                changes.resourceUpdate(machine.getInternalId(), machine);
                internalChangeService.changesExecute(changes);
            } finally {
                if (oldContext != null) {
                    SecurityContextHolder.setContext(oldContext);
                }
            }
        }
    }

}
