/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.chart.Chart;
import com.foilen.chart.ChartBuilder;
import com.foilen.chart.ChartSerieBuilder;
import com.foilen.infra.api.model.DiskStat;
import com.foilen.infra.api.model.SystemStats;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.resource.machine.Machine;
import com.foilen.infra.ui.InfraUiException;
import com.foilen.infra.ui.db.dao.MachineStatisticsDao;
import com.foilen.infra.ui.db.domain.monitoring.MachineStatisticFS;
import com.foilen.infra.ui.db.domain.monitoring.MachineStatisticNetwork;
import com.foilen.infra.ui.db.domain.monitoring.MachineStatistics;
import com.foilen.smalltools.systemusage.results.NetworkInfo;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.JsonTools;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
@Transactional
public class MachineStatisticsServiceImpl implements MachineStatisticsService {

    private static class AggregatedStats {
        private List<String> timestamp = new ArrayList<>();
        private List<Double> cpu = new ArrayList<>();
        private double cpuTotal = 0d;
        private List<Double> memory = new ArrayList<>();
        private double memoryTotal = 0d;
        private List<Double> memorySwap = new ArrayList<>();
        private double memorySwapTotal = 0d;
        private List<Double> disk = new ArrayList<>();
        private double diskTotal = 0d;
        private List<Double> netIn = new ArrayList<>();
        private double netInTotal = 0d;
        private List<Double> netOut = new ArrayList<>();
        private double netOutTotal = 0d;
    }

    private class AggregatedStatsCacheLoader extends CacheLoader<String, AggregatedStats> {

        @Override
        public AggregatedStats load(String machineName) throws Exception {

            logger.debug("Loading stats aggregation for {}", machineName);

            AggregatedStats aggregatedStats = new AggregatedStats();
            Long machineInternalId = findMachineInternalId(machineName);
            if (machineInternalId == null) {
                return aggregatedStats;
            }

            List<MachineStatistics> machineStatistics = machineStatisticsDao.findAllByMachineInternalIdAndTimestampAfterOrderByTimestamp(machineInternalId,
                    DateTools.addDate(new Date(), Calendar.HOUR, -6));

            for (MachineStatistics oneStat : machineStatistics) {
                aggregatedStats.timestamp.add(DateTools.formatFull(oneStat.getTimestamp()));

                aggregatedStats.cpu.add((double) oneStat.getCpuUsed());
                aggregatedStats.cpuTotal = Math.max(aggregatedStats.cpuTotal, oneStat.getCpuTotal());

                aggregatedStats.memory.add((double) oneStat.getMemoryUsed());
                aggregatedStats.memoryTotal = Math.max(aggregatedStats.memoryTotal, oneStat.getMemoryTotal());

                // Memory swap
                aggregatedStats.memorySwap.add((double) oneStat.getMemorySwapUsed());
                aggregatedStats.memorySwapTotal = Math.max(aggregatedStats.memorySwapTotal, oneStat.getMemorySwapTotal());

                double used = 0;
                double total = 0;
                for (MachineStatisticFS fs : oneStat.getFs()) {
                    used += fs.getUsedSpace();
                    total += fs.getTotalSpace();
                }
                used /= 1000000000d;
                total /= 1000000000d;
                aggregatedStats.disk.add(used);
                aggregatedStats.diskTotal = Math.max(aggregatedStats.diskTotal, total);

                double netIn = 0;
                double netOut = 0;
                for (MachineStatisticNetwork net : oneStat.getNetworks()) {
                    netIn += net.getInBytes();
                    netOut += net.getOutBytes();
                }
                aggregatedStats.netIn.add(netIn);
                aggregatedStats.netInTotal = Math.max(aggregatedStats.netInTotal, netIn);
                aggregatedStats.netOut.add(netOut);
                aggregatedStats.netOutTotal = Math.max(aggregatedStats.netOutTotal, netOut);
            }

            return aggregatedStats;
        }

    }

    private static class MachineTimeKey {
        private long machineInternalId;
        private Date timestamp;

        public MachineTimeKey(long machineInternalId, Date timestamp) {
            this.machineInternalId = machineInternalId;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MachineTimeKey other = (MachineTimeKey) obj;
            if (machineInternalId != other.machineInternalId) {
                return false;
            }
            if (timestamp == null) {
                if (other.timestamp != null) {
                    return false;
                }
            } else if (!timestamp.equals(other.timestamp)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (machineInternalId ^ (machineInternalId >>> 32));
            result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
            return result;
        }

    }

    private final static Logger logger = LoggerFactory.getLogger(MachineStatisticsServiceImpl.class);

    @Autowired
    private IPResourceService ipResourceService;

    @Autowired
    private MachineStatisticsDao machineStatisticsDao;
    private LoadingCache<String, AggregatedStats> aggregatedStatsByMachineName = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new AggregatedStatsCacheLoader());

    @Override
    public void addStats(String machineName, List<SystemStats> systemStatsList) {

        // Get the machine
        logger.info("[{}] Adding {} stats", machineName, systemStatsList.size());
        Long machineInternalId = findMachineInternalId(machineName);
        if (machineInternalId == null) {
            logger.error("[{}] The machine does not exists", machineName);
            return;
        }

        // Go through all the stats
        for (SystemStats systemStats : systemStatsList) {
            logger.info("[{}] Processing {}", machineName, JsonTools.compactPrint(systemStats));

            // CPU and Memory
            MachineStatistics machineStatistics = new MachineStatistics( //
                    machineInternalId, //
                    systemStats.getTimestamp(), //
                    systemStats.getCpuUsed(), //
                    systemStats.getCpuTotal(), //
                    systemStats.getMemoryUsed(), //
                    systemStats.getMemoryTotal(), //
                    systemStats.getMemorySwapUsed(), //
                    systemStats.getMemorySwapTotal());

            // Disks
            for (DiskStat diskStat : systemStats.getDiskStats()) {
                machineStatistics.getFs().add(new MachineStatisticFS( //
                        diskStat.getPath(), //
                        diskStat.isRoot(), //
                        diskStat.getUsedSpace(), //
                        diskStat.getTotalSpace()));
            }

            // Networks
            for (NetworkInfo networkInfo : systemStats.getNetworkDeltas()) {
                machineStatistics.getNetworks().add(new MachineStatisticNetwork( //
                        networkInfo.getInterfaceName(), //
                        networkInfo.getInBytes(), //
                        networkInfo.getOutBytes()));
            }

            machineStatisticsDao.save(machineStatistics);
        }

        logger.info("[{}] Adding stats completed", machineName);

    }

    @Override
    public void aggregateByDay() {
        // Get the time of the items to process
        Date before = removeToDays(DateTools.addDate(new Date(), Calendar.WEEK_OF_YEAR, -1));

        logger.info("Will aggregate by day all items before {}", DateTools.formatFull(before));

        // Get the items to process
        List<MachineStatistics> allToProcess = machineStatisticsDao.findAllAggregationByHoursByTimestampBefore(before);
        Map<MachineTimeKey, List<MachineStatistics>> toProcessByKey = allToProcess.stream() //
                .collect(Collectors.groupingBy(it -> //
        new MachineTimeKey( //
                it.getMachineInternalId(), //
                removeToDays(it.getTimestamp()) //
        )));

        // Get or create the existing aggregation for the key times
        Map<MachineTimeKey, MachineStatistics> mergedByMachineAndTime = new HashMap<>();
        for (MachineTimeKey key : toProcessByKey.keySet()) {
            MachineStatistics existing = machineStatisticsDao.findAggregationDayByMachineInternalIdAndTimestamp(key.machineInternalId, key.timestamp);
            if (existing == null) {
                existing = new MachineStatistics(key.machineInternalId, key.timestamp, 0, 0, 0, 0, 0, 0);
            }
            mergedByMachineAndTime.put(key, existing);
        }

        // Average and merge
        for (Entry<MachineTimeKey, List<MachineStatistics>> entry : toProcessByKey.entrySet()) {
            MachineTimeKey key = entry.getKey();
            List<MachineStatistics> statEntry = entry.getValue();
            logger.info("Processing machine {} and time {}", key.machineInternalId, DateTools.formatFull(key.timestamp));
            logger.info("Processing machine {} and time {}. Got {} stats to add", key.machineInternalId, DateTools.formatFull(key.timestamp), statEntry.size());

            MachineStatistics merged = mergedByMachineAndTime.get(key);

            int toAdd = statEntry.stream().collect(Collectors.summingInt(MachineStatistics::getAggregationsForHour));
            int total = averageAndMerge(merged, merged.getAggregationsForDay(), toAdd, statEntry, true);
            merged.setAggregationsForDay(total);
        }

        // Save aggregation and delete processed entries
        machineStatisticsDao.save(mergedByMachineAndTime.values());
        machineStatisticsDao.delete(allToProcess);
    }

    @Override
    public void aggregateByHour() {
        // Get the time of the items to process
        Date before = removeToHours(DateTools.addDate(new Date(), Calendar.HOUR, -6));

        logger.info("Will aggregate by hour all items before {}", DateTools.formatFull(before));

        // Get the items to process
        List<MachineStatistics> allToProcess = machineStatisticsDao.findAllNonAggregationByTimestampBefore(before);
        Map<MachineTimeKey, List<MachineStatistics>> toProcessByKey = allToProcess.stream() //
                .collect(Collectors.groupingBy(it -> //
        new MachineTimeKey( //
                it.getMachineInternalId(), //
                removeToHours(it.getTimestamp()) //
        )));

        // Get or create the existing aggregation for the key times
        Map<MachineTimeKey, MachineStatistics> mergedByMachineAndTime = new HashMap<>();
        for (MachineTimeKey key : toProcessByKey.keySet()) {
            MachineStatistics existing = machineStatisticsDao.findAggregationHourByMachineInternalIdAndTimestamp(key.machineInternalId, key.timestamp);
            if (existing == null) {
                existing = new MachineStatistics(key.machineInternalId, key.timestamp, 0, 0, 0, 0, 0, 0);
            }
            mergedByMachineAndTime.put(key, existing);
        }

        // Average and merge
        for (Entry<MachineTimeKey, List<MachineStatistics>> entry : toProcessByKey.entrySet()) {
            MachineTimeKey key = entry.getKey();
            List<MachineStatistics> statEntry = entry.getValue();
            logger.info("Processing machine {} and time {}. Got {} stats to add", key.machineInternalId, DateTools.formatFull(key.timestamp), statEntry.size());

            MachineStatistics merged = mergedByMachineAndTime.get(key);
            int total = averageAndMerge(merged, merged.getAggregationsForHour(), statEntry.size(), statEntry, false);
            merged.setAggregationsForHour(total);
        }

        // Save aggregation and delete processed entries
        machineStatisticsDao.save(mergedByMachineAndTime.values());
        machineStatisticsDao.delete(allToProcess);
    }

    private int averageAndMerge(MachineStatistics merged, int currentCount, int toAdd, List<MachineStatistics> toProcess, boolean multWithAggregatedHours) {

        // Adjust current values
        int total = currentCount + toAdd;
        logger.debug("Total = {} + {} = {}", currentCount, toAdd, total);

        logger.debug("Initial merge {}", merged);
        multiplyStat(merged, currentCount);
        logger.debug("Merge after multiplication {}", merged);

        // Add all the new values
        for (MachineStatistics next : toProcess) {
            if (multWithAggregatedHours) {
                logger.debug("Multiplying stat {}", next);
                multiplyStat(next, next.getAggregationsForHour());
                logger.debug("After multiplication {}", next);
            }
            logger.debug("Adding stat {}", next);
            merged.setCpuUsed(merged.getCpuUsed() + next.getCpuUsed());
            merged.setCpuTotal(Math.max(merged.getCpuTotal(), next.getCpuTotal()));
            merged.setMemoryUsed(merged.getMemoryUsed() + next.getMemoryUsed());
            merged.setMemoryTotal(Math.max(merged.getMemoryTotal(), next.getMemoryTotal()));
            merged.setMemorySwapUsed(merged.getMemorySwapUsed() + next.getMemorySwapUsed());
            merged.setMemorySwapTotal(Math.max(merged.getMemorySwapTotal(), next.getMemorySwapTotal()));

            for (MachineStatisticFS nextFs : next.getFs()) {
                // Find or create in merge
                Optional<MachineStatisticFS> optionalMergedFs = merged.getFs().stream().filter(it -> it.getPath().equals(nextFs.getPath())).findAny();
                MachineStatisticFS mergedFs;
                if (optionalMergedFs.isPresent()) {
                    mergedFs = optionalMergedFs.get();
                } else {
                    mergedFs = new MachineStatisticFS(nextFs.getPath(), nextFs.isRoot(), 0, 0);
                    merged.getFs().add(mergedFs);
                }

                mergedFs.setUsedSpace(Math.max(mergedFs.getUsedSpace(), nextFs.getUsedSpace()));
                mergedFs.setTotalSpace(Math.max(mergedFs.getTotalSpace(), nextFs.getTotalSpace()));
            }
            for (MachineStatisticNetwork nextNet : next.getNetworks()) {
                // Find or create in merge
                Optional<MachineStatisticNetwork> optionalMergedNet = merged.getNetworks().stream().filter(it -> it.getInterfaceName().equals(nextNet.getInterfaceName())).findAny();
                MachineStatisticNetwork mergedNet;
                if (optionalMergedNet.isPresent()) {
                    mergedNet = optionalMergedNet.get();
                } else {
                    mergedNet = new MachineStatisticNetwork(nextNet.getInterfaceName(), 0, 0);
                    merged.getNetworks().add(mergedNet);
                }

                mergedNet.setInBytes(mergedNet.getInBytes() + nextNet.getInBytes());
                mergedNet.setOutBytes(mergedNet.getOutBytes() + nextNet.getOutBytes());
            }
            logger.debug("Merge after addition {}", merged);
        }

        // Divide by total
        merged.setCpuUsed(merged.getCpuUsed() / total);
        merged.setMemoryUsed(merged.getMemoryUsed() / total);
        merged.setMemorySwapUsed(merged.getMemorySwapUsed() / total);
        for (MachineStatisticNetwork net : merged.getNetworks()) {
            net.setInBytes(net.getInBytes() / total);
            net.setOutBytes(net.getOutBytes() / total);
        }
        logger.debug("Merge after division {}", merged);

        return total;
    }

    private Long findMachineInternalId(String machineName) {
        Optional<Machine> machine = ipResourceService.resourceFind(ipResourceService.createResourceQuery(Machine.class) //
                .propertyEquals(Machine.PROPERTY_NAME, machineName));
        if (machine.isPresent()) {
            return machine.get().getInternalId();
        } else {
            return null;
        }
    }

    private AggregatedStats getAggregatedStats(String machineName) {
        try {
            return aggregatedStatsByMachineName.get(machineName);
        } catch (ExecutionException e) {
            throw new InfraUiException("Problem aggregating the stats for " + machineName, e);
        }
    }

    private Chart getChart(String serieName, String color, String backgroundColor, Double total, List<String> timestamp, List<Double> values) {
        ChartBuilder chartBuilder = new ChartBuilder();
        ChartSerieBuilder cpuSerie = chartBuilder.addSerie(serieName).setMax(total).setColor(color).setBackgroundColor(backgroundColor);
        for (int i = 0; i < timestamp.size(); ++i) {
            chartBuilder.addX(timestamp.get(i));
            cpuSerie.addY(values.get(i));
        }
        return chartBuilder.built();
    }

    @Override
    public Chart getCpuChart(String machineName) {
        AggregatedStats aggregatedStats = getAggregatedStats(machineName);
        return getChart("CPU", "rgba(153, 255, 153, 1)", "rgba(153, 255, 153, 0.5)", aggregatedStats.cpuTotal, aggregatedStats.timestamp, aggregatedStats.cpu);
    }

    @Override
    public Chart getDiskChart(String machineName) {
        AggregatedStats aggregatedStats = getAggregatedStats(machineName);
        return getChart("Disk / (GB)", "rgba(255, 127, 35, 1)", "rgba(255, 127, 35, 0.5)", aggregatedStats.diskTotal, aggregatedStats.timestamp, aggregatedStats.disk);
    }

    @Override
    public Chart getMemoryChart(String machineName) {
        AggregatedStats aggregatedStats = getAggregatedStats(machineName);

        ChartBuilder chartBuilder = new ChartBuilder();
        double total = aggregatedStats.memorySwapTotal + aggregatedStats.memoryTotal;
        ChartSerieBuilder physicalSerie = chartBuilder.addSerie("Memory").setMax(total).setColor("rgba(76, 165, 255, 1)").setBackgroundColor("rgba(76, 165, 255, 0.5)");
        ChartSerieBuilder swapAndPhysicalSerie = chartBuilder.addSerie("Memory with SWAP").setMax(total).setColor("rgba(255, 0, 0, 1)").setBackgroundColor("rgba(255, 0, 0, 0.5)");
        for (int i = 0; i < aggregatedStats.timestamp.size(); ++i) {
            chartBuilder.addX(aggregatedStats.timestamp.get(i));
            swapAndPhysicalSerie.addY(aggregatedStats.memorySwap.get(i) + aggregatedStats.memory.get(i));
            physicalSerie.addY(aggregatedStats.memory.get(i));
        }
        return chartBuilder.built();
    }

    @Override
    public Chart getNetworkChart(String machineName) {
        AggregatedStats aggregatedStats = getAggregatedStats(machineName);

        ChartBuilder chartBuilder = new ChartBuilder();
        ChartSerieBuilder inSerie = chartBuilder.addSerie("IN").setMax(aggregatedStats.netInTotal).setColor("rgba(153, 255, 153, 1)").setBackgroundColor("rgba(153, 255, 153, 0.5)");
        ChartSerieBuilder outSerie = chartBuilder.addSerie("OUT").setMax(aggregatedStats.netOutTotal).setColor("rgba(76, 165, 255, 1)").setBackgroundColor("rgba(76, 165, 255, 0.5)");
        for (int i = 0; i < aggregatedStats.timestamp.size(); ++i) {
            chartBuilder.addX(aggregatedStats.timestamp.get(i));
            inSerie.addY(aggregatedStats.netIn.get(i));
            outSerie.addY(aggregatedStats.netOut.get(i));
        }
        return chartBuilder.built();
    }

    private void multiplyStat(MachineStatistics machineStatistics, int factor) {
        machineStatistics.setCpuUsed(machineStatistics.getCpuUsed() * factor);
        machineStatistics.setMemoryUsed(machineStatistics.getMemoryUsed() * factor);
        machineStatistics.setMemorySwapUsed(machineStatistics.getMemorySwapUsed() * factor);
        for (MachineStatisticNetwork net : machineStatistics.getNetworks()) {
            net.setInBytes(net.getInBytes() * factor);
            net.setOutBytes(net.getOutBytes() * factor);
        }
    }

    private Date removeToDays(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        return calendar.getTime();
    }

    private Date removeToHours(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        return calendar.getTime();
    }

}
