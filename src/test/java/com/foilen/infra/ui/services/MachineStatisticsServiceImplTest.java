/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.machine.SystemStats;
import com.foilen.infra.plugin.core.system.mongodb.repositories.PluginResourceRepository;
import com.foilen.infra.plugin.core.system.mongodb.repositories.documents.PluginResource;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.repositories.MachineStatisticsRepository;
import com.foilen.infra.ui.repositories.documents.MachineStatistics;
import com.foilen.infra.ui.repositories.documents.models.MachineStatisticFS;
import com.foilen.infra.ui.repositories.documents.models.MachineStatisticNetwork;
import com.foilen.infra.ui.test.AbstractSpringTests;
import com.foilen.smalltools.reflection.BeanPropertiesCopierTools;
import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.JsonTools;

@Transactional
public class MachineStatisticsServiceImplTest extends AbstractSpringTests {

    @Autowired
    private MachineStatisticsRepository machineStatisticsRepository;
    @Autowired
    private MachineStatisticsService machineStatisticsService;
    @Autowired
    private PluginResourceRepository pluginResourceRepository;

    public MachineStatisticsServiceImplTest() {
        super(true);
    }

    private void assertDb(String jsonFile) {
        List<MachineStatistics> machineStatisticsList = machineStatisticsRepository.findAll(Sort.by("timestamp", "machineInternalId"));

        List<MachineStatistics> actual = new ArrayList<>();
        for (MachineStatistics machineStatistics : machineStatisticsList) {
            actual.add(copy(machineStatistics));
        }

        AssertTools.assertJsonComparison(jsonFile, this.getClass(), actual);

    }

    @Before
    public void clearStats() {
        machineStatisticsRepository.deleteAll();
    }

    private MachineStatisticFS copy(MachineStatisticFS machineStatisticFS) {
        BeanPropertiesCopierTools bp = new BeanPropertiesCopierTools(machineStatisticFS, MachineStatisticFS.class);
        bp.copyProperty("path");
        bp.copyProperty("root");
        bp.copyProperty("usedSpace");
        bp.copyProperty("totalSpace");
        return (MachineStatisticFS) bp.getDestination();
    }

    private MachineStatisticNetwork copy(MachineStatisticNetwork machineStatisticNetwork) {
        BeanPropertiesCopierTools bp = new BeanPropertiesCopierTools(machineStatisticNetwork, MachineStatisticNetwork.class);
        bp.copyProperty("interfaceName");
        bp.copyProperty("inBytes");
        bp.copyProperty("outBytes");
        return (MachineStatisticNetwork) bp.getDestination();
    }

    private MachineStatistics copy(MachineStatistics machineStatistics) {
        BeanPropertiesCopierTools bp = new BeanPropertiesCopierTools(machineStatistics, MachineStatistics.class);
        bp.copyProperty("timestamp");
        bp.copyProperty("cpuUsed");
        bp.copyProperty("cpuTotal");
        bp.copyProperty("memoryUsed");
        bp.copyProperty("memoryTotal");
        bp.copyProperty("aggregationsForHour");
        bp.copyProperty("aggregationsForDay");

        MachineStatistics destination = (MachineStatistics) bp.getDestination();

        // Machine name
        PluginResource pluginResource = pluginResourceRepository.findById(machineStatistics.getMachineInternalId()).get();
        Machine machine = (Machine) pluginResource.getResource();
        destination.setMachineInternalId(machine.getName());

        // FS
        for (MachineStatisticFS machineStatisticFS : machineStatistics.getFs().stream().sorted().collect(Collectors.toList())) {
            destination.getFs().add(copy(machineStatisticFS));
        }

        // Network
        for (MachineStatisticNetwork machineStatisticNetwork : machineStatistics.getNetworks().stream().sorted().collect(Collectors.toList())) {
            destination.getNetworks().add(copy(machineStatisticNetwork));
        }

        return destination;
    }

    @Test
    public void testAddStats() {
        String machineName = "f001.node.example.com";
        List<SystemStats> systemStats = new ArrayList<>();
        systemStats.add(JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass()));
        systemStats.add(JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-2.json", SystemStats.class, this.getClass()));
        machineStatisticsService.addStats(machineName, systemStats);

        Assert.assertEquals(2, machineStatisticsRepository.count());

        assertDb("MachineStatisticsServiceImplTest-testAddStats-expected.json");

    }

    @Test
    public void testAggregation() {
        // Add some stats
        String machineName1 = "f001.node.example.com";
        String machineName2 = "f002.node.example.com";
        List<SystemStats> systemStatsToAdd = new ArrayList<>();
        SystemStats systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-01 1:34:00"));
        systemStats.setCpuUsed(100);
        systemStatsToAdd.add(systemStats);
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-01 1:56:00"));
        systemStats.setCpuUsed(200);
        systemStatsToAdd.add(systemStats);
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-01 3:43:00"));
        systemStats.setCpuUsed(251);
        systemStatsToAdd.add(systemStats);
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-02 7:22:00"));
        systemStats.setCpuUsed(843);
        systemStatsToAdd.add(systemStats);
        machineStatisticsService.addStats(machineName1, systemStatsToAdd);

        systemStatsToAdd.clear();
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-02 7:33:00"));
        systemStats.setCpuUsed(275);
        systemStatsToAdd.add(systemStats);
        machineStatisticsService.addStats(machineName2, systemStatsToAdd);

        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-01.json");

        // Aggregate per hour
        machineStatisticsService.aggregateByHour();
        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-02.json");

        // Aggregate per day
        machineStatisticsService.aggregateByDay();
        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-03.json");

        // Add some stats
        systemStatsToAdd.clear();
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-01 1:12:00"));
        systemStats.setCpuUsed(800);
        systemStatsToAdd.add(systemStats);
        machineStatisticsService.addStats(machineName1, systemStatsToAdd);

        // Aggregate per hour
        machineStatisticsService.aggregateByHour();
        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-04.json");

        // Add some stats
        systemStatsToAdd.clear();
        systemStats = JsonTools.readFromResource("MachineStatisticsServiceImplTest-systemstats-1.json", SystemStats.class, this.getClass());
        systemStats.setTimestamp(DateTools.parseFull("2000-01-01 1:13:00"));
        systemStats.setCpuUsed(900);
        systemStatsToAdd.add(systemStats);
        machineStatisticsService.addStats(machineName1, systemStatsToAdd);

        // Aggregate per hour
        machineStatisticsService.aggregateByHour();
        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-05.json");

        // Aggregate per day
        machineStatisticsService.aggregateByDay();
        // Check
        assertDb("MachineStatisticsServiceImplTest-testAggregation-06.json");
    }

}
