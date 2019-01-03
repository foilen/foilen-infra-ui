/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.List;

import com.foilen.infra.ui.db.domain.reporting.ReportCount;
import com.foilen.infra.ui.db.domain.reporting.ReportTime;

public interface ReportService {

    void addReport(String txId, boolean success, List<ReportTime> reportTimes, List<ReportCount> reportCounts);

}
