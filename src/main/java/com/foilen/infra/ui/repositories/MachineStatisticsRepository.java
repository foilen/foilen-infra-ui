/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.foilen.infra.ui.repositories.documents.MachineStatistics;

public interface MachineStatisticsRepository extends MongoRepository<MachineStatistics, String> {

    @Query("{ 'machineInternalId' : ?0 , 'timestamp' : ?1 , 'aggregationsForDay' : { $ne : 0 } }")
    MachineStatistics findAggregationDayByMachineInternalIdAndTimestamp(String machineInternalId, Date timestamp);

    @Query("{ 'machineInternalId' : ?0 , 'timestamp' : ?1 , 'aggregationsForHour' : { $ne : 0 } }")
    MachineStatistics findAggregationHourByMachineInternalIdAndTimestamp(String machineInternalId, Date timestamp);

    @Query("{ 'timestamp' : { $lt : ?0 }  , 'aggregationsForHour' : { $ne : 0 } , 'aggregationsForDay' : 0  }")
    List<MachineStatistics> findAllAggregationByHoursByTimestampBefore(Date timestamp);

    List<MachineStatistics> findAllByMachineInternalIdAndTimestampAfterOrderByTimestamp(String machineInternalId, Date addDate);

    @Query("{ 'timestamp' : { $lt : ?0 }  , 'aggregationsForHour' : 0 , 'aggregationsForDay' : 0  }")
    List<MachineStatistics> findAllNonAggregationByTimestampBefore(Date beforeTimestamp);

}
