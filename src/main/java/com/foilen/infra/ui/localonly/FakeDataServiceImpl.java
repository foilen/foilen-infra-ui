/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.localonly;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.DiskStat;
import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.plugin.v1.core.base.editors.UnixUserEditor;
import com.foilen.infra.plugin.v1.core.base.resources.Application;
import com.foilen.infra.plugin.v1.core.base.resources.Machine;
import com.foilen.infra.plugin.v1.core.base.resources.UnixUser;
import com.foilen.infra.plugin.v1.core.base.resources.helper.UnixUserAvailableIdHelper;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.plugin.v1.model.resource.LinkTypeConstants;
import com.foilen.infra.ui.db.dao.ApiUserDao;
import com.foilen.infra.ui.db.dao.AuditItemDao;
import com.foilen.infra.ui.db.dao.MachineStatisticsDao;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.dao.PluginResourceLinkDao;
import com.foilen.infra.ui.db.dao.PluginResourceTagDao;
import com.foilen.infra.ui.db.dao.UserDao;
import com.foilen.infra.ui.db.domain.user.User;
import com.foilen.infra.ui.services.MachineStatisticsService;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.DateTools;

@Transactional
public class FakeDataServiceImpl implements FakeDataService {

    public static final String USER_ID_ADMIN = "111111";
    public static final String USER_ID_USER = "222222";
    public static final String USER_ID_TEST_1 = "333333";

    public static final String PASSWORD_HASH_QWERTY = "$6$rrH5iqhf$u3IId7XXX7HhG3O2NlSXIyLSSQ3NCbmpyWVcZV/NiqpCnf1ryQnXcE./Dr5A5rehosGm/ppFPFssCD5U4dfkB.";

    private final static Logger logger = LoggerFactory.getLogger(FakeDataServiceImpl.class);

    @Autowired
    private ApiUserDao apiUserDao;
    @Autowired
    private AuditItemDao auditItemDao;
    @Autowired
    private InternalChangeService internalChangeService;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private MachineStatisticsDao machineStatisticsDao;
    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private PluginResourceDao pluginResourceDao;
    @Autowired
    private PluginResourceLinkDao pluginResourceLinkDao;
    @Autowired
    private PluginResourceTagDao pluginResourceTagDao;
    @Autowired
    private PluginResourceColumnSearchDao pluginResourceColumnSearchDao;
    @Autowired
    private UserDao userDao;

    @Override
    public void clearAll() {

        logger.info("Begin CLEAR ALL");

        apiUserDao.deleteAll();
        auditItemDao.deleteAll();

        machineStatisticsDao.deleteAll();

        pluginResourceTagDao.deleteAll();

        pluginResourceColumnSearchDao.deleteAll();
        pluginResourceLinkDao.deleteAll();
        pluginResourceDao.deleteAll();

        userDao.deleteAll();

        userDao.flush();

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

        UnixUser uuNotAttached = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "notattached", "/home/notattached", "/bin/bash", null);
        UnixUser uuIndirectlyAttached = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "indirectlyattached", "/home/indirectlyattached", "/bin/bash", null);
        UnixUser uuF1 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f1", "/home/f1", "/bin/bash", PASSWORD_HASH_QWERTY);
        UnixUser uuF2 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f2", "/home/f2", "/bin/bash", null);
        UnixUser uuF12 = new UnixUser(UnixUserAvailableIdHelper.getNextAvailableId(), "f12", "/home/f12", "/bin/bash", PASSWORD_HASH_QWERTY);
        setResourceEditor(UnixUserEditor.EDITOR_NAME, uuNotAttached, uuIndirectlyAttached, uuF1, uuF2, uuF12);

        ChangesContext changes = new ChangesContext(ipResourceService);
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

        userDao.saveAndFlush(new User(USER_ID_ADMIN, true));
        userDao.saveAndFlush(new User(USER_ID_USER, false));
        userDao.saveAndFlush(new User(USER_ID_TEST_1, false));
        userDao.saveAndFlush(new User("444444", false));
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
