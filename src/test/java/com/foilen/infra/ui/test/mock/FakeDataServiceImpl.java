/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.test.mock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.DiskStat;
import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceLinkRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.resource.application.Application;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.resource.unixuser.SystemUnixUser;
import com.foilen.infra.resource.unixuser.UnixUser;
import com.foilen.infra.resource.unixuser.UnixUserEditor;
import com.foilen.infra.resource.unixuser.helper.UnixUserAvailableIdHelper;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.MachineStatisticsRepository;
import com.foilen.infra.ui.repositories.ReportExecutionRepository;
import com.foilen.infra.ui.repositories.UserApiMachineRepository;
import com.foilen.infra.ui.repositories.UserApiRepository;
import com.foilen.infra.ui.repositories.UserHumanRepository;
import com.foilen.infra.ui.repositories.documents.UserApi;
import com.foilen.infra.ui.repositories.documents.UserApiMachine;
import com.foilen.infra.ui.repositories.documents.UserHuman;
import com.foilen.infra.ui.services.MachineStatisticsService;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;

@Service
@Transactional
public class FakeDataServiceImpl extends AbstractBasics implements FakeDataService {

    public static final String USER_ID_ADMIN = "111111";
    public static final String USER_ID_USER = "222222";
    public static final String USER_ID_TEST_1 = "333333";

    public static final String API_USER_MACHINE_ID_F001 = "MF001";
    public static final String API_USER_MACHINE_ID_F002 = "MF002";

    public static final String API_USER_ID_ADMIN = "AADMIN";
    public static final String API_USER_ID_USER = "AUSER";

    public static final String API_PASSWORD = "01234567";
    public static final String API_PASSWORD_HASH = "$2a$13$CXFWBseE7qnfRndtunCj/OoQgLLz7AOe2e3aNdNFwmqkTQfoduVMy";

    public static final String PASSWORD_HASH_QWERTY = "$6$rrH5iqhf$u3IId7XXX7HhG3O2NlSXIyLSSQ3NCbmpyWVcZV/NiqpCnf1ryQnXcE./Dr5A5rehosGm/ppFPFssCD5U4dfkB.";

    @Autowired
    private AuditItemRepository auditItemRepository;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private MachineStatisticsRepository machineStatisticsRepository;
    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;
    @Autowired
    private PluginResourceLinkRepository pluginResourceLinkRepository;
    @Autowired
    private ReportExecutionRepository reportExecutionRepository;
    @Autowired
    private UserApiRepository userApiRepository;
    @Autowired
    private UserApiMachineRepository userApiMachineRepository;
    @Autowired
    private UserHumanRepository userHumanRepository;

    @Override
    public void clearAll() {

        logger.info("Begin CLEAR ALL");

        reportExecutionRepository.deleteAll();

        userApiMachineRepository.deleteAll();
        userApiRepository.deleteAll();
        auditItemRepository.deleteAll();

        machineStatisticsRepository.deleteAll();

        pluginResourceLinkRepository.deleteAll();
        pluginResourceRepository.deleteAll();

        userHumanRepository.deleteAll();

        logger.info("End CLEAR ALL");
    }

    @Override
    public void createAll() {

        logger.info("Begin CREATE ALL");

        createUsers();
        createMachines();
        createMachineStatistics();

        createUnixUsers();
        createApplications();

        logger.info("End CREATE ALL");
    }

    protected Application createApplication(String name, String description) {
        Application application = new Application();
        application.setName(name);
        application.setDescription(description);
        return application;
    }

    public void createApplications() {

        logger.info("createApplications");

        Application aNotAttached = createApplication("notattached", "A not attached application");
        Application f1 = createApplication("f1", "Attached to machine f001");
        Application f1Indirect = createApplication("f1indirect", "Attached to machine f001 and attaching another unixuser");

        ChangesContext changes = new ChangesContext(ipResourceService);
        changes.resourceAdd(aNotAttached);
        changes.resourceAdd(f1);
        changes.linkAdd(f1, LinkTypeConstants.INSTALLED_ON, new Machine("f001.node.example.com"));
        changes.linkAdd(f1, LinkTypeConstants.RUN_AS, findUnixUser("f1"));
        changes.resourceAdd(f1Indirect);
        changes.linkAdd(f1Indirect, LinkTypeConstants.INSTALLED_ON, new Machine("f001.node.example.com"));
        changes.linkAdd(f1Indirect, LinkTypeConstants.RUN_AS, findUnixUser("indirectlyattached"));
        internalChangeService.changesExecute(changes);

    }

    public void createMachines() {
        logger.info("createMachines");

        ChangesContext changes = new ChangesContext(ipResourceService);
        changes.resourceAdd(new Machine("f001.node.example.com"));
        changes.resourceAdd(new Machine("f002.node.example.com"));
        internalChangeService.changesExecute(changes);

        userApiMachineRepository.save(new UserApiMachine(API_USER_MACHINE_ID_F001, API_PASSWORD, API_PASSWORD_HASH, "f001.node.example.com", DateTools.addDate(Calendar.DAY_OF_YEAR, 30)));
        userApiMachineRepository.save(new UserApiMachine(API_USER_MACHINE_ID_F002, API_PASSWORD, API_PASSWORD_HASH, "f002.node.example.com", DateTools.addDate(Calendar.DAY_OF_YEAR, 30)));
    }

    public void createMachineStatistics() {
        logger.info("createMachineStatistics");

        ipResourceService.resourceFindAll(ipResourceService.createResourceQuery(Machine.class)).stream() //
                .forEach(machine -> {
                    List<SystemStats> systemStats = new ArrayList<>();
                    for (int minutesAgo = -60; minutesAgo < 0; ++minutesAgo) {
                        SystemStats systemStat = new SystemStats();
                        systemStat.setTimestamp(DateTools.addDate(Calendar.MINUTE, minutesAgo));
                        systemStat.setCpuTotal(1000);
                        systemStat.setCpuUsed((long) (Math.random() * 1000));
                        systemStat.setMemorySwapTotal(4096);
                        systemStat.setMemorySwapUsed((long) (Math.random() * 4096));
                        systemStat.setMemoryTotal(8192);
                        systemStat.setMemoryUsed((long) (Math.random() * (systemStat.getMemoryTotal() - systemStat.getMemorySwapTotal()) + systemStat.getMemorySwapTotal()));

                        List<DiskStat> diskStats = new ArrayList<>();
                        DiskStat diskRootStat = new DiskStat();
                        diskRootStat.setRoot(true);
                        diskRootStat.setPath("/");
                        diskRootStat.setTotalSpace(200000000000l);
                        diskRootStat.setFreeSpace((long) (Math.random() * 200000000000l));
                        diskRootStat.setFreeSpacePercent(diskRootStat.getFreeSpace() / diskRootStat.getTotalSpace());
                        diskRootStat.setUsedSpace(diskRootStat.getTotalSpace() - diskRootStat.getFreeSpace());
                        diskRootStat.setUsedSpacePercent(diskRootStat.getUsedSpace() / diskRootStat.getTotalSpace());
                        diskStats.add(diskRootStat);
                        DiskStat diskBootStat = new DiskStat();
                        diskBootStat.setRoot(true);
                        diskBootStat.setPath("/boot");
                        diskBootStat.setTotalSpace(1000000000l);
                        diskBootStat.setFreeSpace(800000000);
                        diskBootStat.setFreeSpacePercent(diskBootStat.getFreeSpace() / diskBootStat.getTotalSpace());
                        diskBootStat.setUsedSpace(diskBootStat.getTotalSpace() - diskBootStat.getFreeSpace());
                        diskBootStat.setUsedSpacePercent(diskBootStat.getUsedSpace() / diskBootStat.getTotalSpace());
                        diskStats.add(diskBootStat);
                        systemStat.setDiskStats(diskStats);

                        List<NetworkInfo> networkDeltas = new ArrayList<>();
                        NetworkInfo networkInfo = new NetworkInfo();
                        networkInfo.setInterfaceName("eth0");
                        networkInfo.setInBytes((long) (Math.random() * 1024000));
                        networkInfo.setInPackets(networkInfo.getInBytes() / 1024);
                        networkInfo.setOutBytes((long) (Math.random() * 1024000));
                        networkInfo.setOutPackets(networkInfo.getOutBytes() / 1024);
                        networkDeltas.add(networkInfo);
                        systemStat.setNetworkDeltas(networkDeltas);

                        systemStats.add(systemStat);
                    }

                    machineStatisticsService.addStats(machine.getName(), systemStats);
                });

    }

    public void createUnixUsers() {
        logger.info("createUnixUsers");

        UnixUserAvailableIdHelper.init(ipResourceService);

        SystemUnixUser root = new SystemUnixUser(0L, "root");
        UnixUser uuNotAttached = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "notattached", "/home/notattached", "/bin/bash", null);
        UnixUser uuIndirectlyAttached = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "indirectlyattached", "/home/indirectlyattached", "/bin/bash", null);
        UnixUser uuF1 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f1", "/home/f1", "/bin/bash", PASSWORD_HASH_QWERTY);
        UnixUser uuF2 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f2", "/home/f2", "/bin/bash", null);
        UnixUser uuF12 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f12", "/home/f12", "/bin/bash", PASSWORD_HASH_QWERTY);
        setResourceEditor(UnixUserEditor.EDITOR_NAME, uuNotAttached, uuIndirectlyAttached, uuF1, uuF2, uuF12);

        ChangesContext changes = new ChangesContext(ipResourceService);
        changes.resourceAdd(root);
        changes.resourceAdd(uuNotAttached);
        changes.resourceAdd(uuIndirectlyAttached);
        changes.resourceAdd(uuF1);
        changes.resourceAdd(uuF2);
        changes.resourceAdd(uuF12);
        changes.linkAdd(uuF1, LinkTypeConstants.INSTALLED_ON, new Machine("f001.node.example.com"));
        changes.linkAdd(uuF12, LinkTypeConstants.INSTALLED_ON, new Machine("f001.node.example.com"));
        changes.linkAdd(uuF2, LinkTypeConstants.INSTALLED_ON, new Machine("f002.node.example.com"));
        changes.linkAdd(uuF12, LinkTypeConstants.INSTALLED_ON, new Machine("f002.node.example.com"));
        internalChangeService.changesExecute(changes);

    }

    public void createUsers() {
        logger.info("createUsers");

        userHumanRepository.save(new UserHuman(USER_ID_ADMIN, true));
        userHumanRepository.save(new UserHuman(USER_ID_USER, false));
        userHumanRepository.save(new UserHuman(USER_ID_TEST_1, false));
        userHumanRepository.save(new UserHuman("444444", false));

        userApiRepository.save(new UserApi(API_USER_ID_ADMIN, API_PASSWORD_HASH, "An admin").setAdmin(true));
        userApiRepository.save(new UserApi(API_USER_ID_USER, API_PASSWORD_HASH, "A normal user"));
    }

    protected UnixUser findUnixUser(String name) {
        return ipResourceService.resourceFind(ipResourceService.createResourceQuery(UnixUser.class).propertyEquals(UnixUser.PROPERTY_NAME, name)).get();
    }

    private void setResourceEditor(String editorName, IPResource... resources) {
        for (IPResource resource : resources) {
            resource.setResourceEditorName(editorName);
        }
    }

}
