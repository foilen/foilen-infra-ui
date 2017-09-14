/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
 package com.foilen.infra.ui.db.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.db.domain.monitoring.MachineStatistics;

@Service
public interface MachineStatisticsDao extends JpaRepository<MachineStatistics, Long> {

    @Query("SELECT ms FROM MachineStatistics ms WHERE " + //
            " ms.machineInternalId = :machineInternalId " + //
            " AND ms.timestamp = :timestamp " + //
            " AND aggregationsForDay != 0")
    MachineStatistics findAggregationDayByMachineInternalIdAndTimestamp(@Param("machineInternalId") long machineInternalId, @Param("timestamp") Date timestamp);

    @Query("SELECT ms FROM MachineStatistics ms WHERE " + //
            " ms.machineInternalId = :machineInternalId " + //
            " AND ms.timestamp = :timestamp " + //
            " AND aggregationsForHour != 0")
    MachineStatistics findAggregationHourByMachineInternalIdAndTimestamp(@Param("machineInternalId") long machineInternalId, @Param("timestamp") Date timestamp);

    @Query("SELECT ms FROM MachineStatistics ms WHERE " //
            + " ms.timestamp < :timestamp " //
            + " AND aggregationsForHour != 0 " //
            + " AND aggregationsForDay = 0 ")
    List<MachineStatistics> findAllAggregationByHoursByTimestampBefore(@Param("timestamp") Date timestamp);

    List<MachineStatistics> findAllByMachineInternalIdAndTimestampAfterOrderByTimestamp(Long machineInternalId, Date addDate);

    @Query("SELECT ms FROM MachineStatistics ms WHERE " //
            + " ms.timestamp < :timestamp " //
            + " AND aggregationsForHour = 0 " //
            + " AND aggregationsForDay = 0 ")
    List<MachineStatistics> findAllNonAggregationByTimestampBefore(@Param("timestamp") Date beforeTimestamp);

}
