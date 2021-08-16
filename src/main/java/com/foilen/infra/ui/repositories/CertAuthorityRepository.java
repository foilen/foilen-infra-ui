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

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.ui.repositories.documents.CertAuthority;

@Service
public interface CertAuthorityRepository extends MongoRepository<CertAuthority, String> {

    void deleteAllByIdNotIn(List<String> usedCertAuthIds);

    List<CertAuthority> findAllByEndDateBefore(Date endDate);

    List<CertAuthority> findAllByNameOrderByStartDate(String name);

    @Aggregation({ //
            "{ $group : { _id : $name, maxEndDate : { $max : $endDate } } }", //
            "{ $match : { maxEndDate : { $lt: ?0 } }}", //
            "{ $project : { _id : '$_id' } }" } //
    )
    List<String> findAllNameByLatestEndDateBefore(Date maxEndDateBefore);

}
