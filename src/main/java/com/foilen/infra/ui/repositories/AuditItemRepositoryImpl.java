/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.repositories;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.request.RequestAuditItem;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.infra.ui.services.PaginationService;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.DateTools;
import com.google.common.base.Strings;

@Service
@Transactional
public class AuditItemRepositoryImpl extends AbstractBasics implements AuditItemCustomRepository {

    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private PaginationService paginationService;

    @Override
    public Page<AuditItem> findAll(RequestAuditItem request) {

        Query query = new Query();

        // Sorting & Page
        PageRequest pageRequest = PageRequest.of(request.getPageId() - 1, paginationService.getItemsPerPage(), Direction.DESC, "timestamp", "id");
        query.with(pageRequest);

        // Filters
        if (!Strings.isNullOrEmpty(request.getTimestampFrom())) {
            query.addCriteria(Criteria.where("timestamp").gte(DateTools.parseFull(request.getTimestampFrom())));
        }
        if (!Strings.isNullOrEmpty(request.getTimestampTo())) {
            query.addCriteria(Criteria.where("timestamp").lte(DateTools.parseFull(request.getTimestampTo())));
        }

        notNullValue(query, "txId", request.getTxId());

        if (request.getExplicitChange() != null) {
            query.addCriteria(Criteria.where("explicitChange").is(request.getExplicitChange()));
        }

        notNullValue(query, "type", request.getType());
        notNullValue(query, "action", request.getAction());

        notNullValue(query, "userType", request.getUserType());
        notNullValue(query, "userName", request.getUserName());

        notNullValue(query, "resourceFirstType", request.getResourceFirstType());
        notNullValue(query, "resourceSecondType", request.getResourceSecondType());

        notNullValue(query, "linkType", request.getLinkType());
        notNullValue(query, "tagName", request.getTagName());

        notNullValue(query, "documentType", request.getDocumentType());
        notNullValue(query, "documentId", request.getDocumentId());

        // Make the request
        List<AuditItem> results = mongoOperations.find(query, AuditItem.class);
        return PageableExecutionUtils.getPage(results, pageRequest, () -> mongoOperations.count(Query.of(query).limit(-1).skip(-1), AuditItem.class));
    }

    private void notNullValue(Query query, String field, Object value) {

        if (value == null) {
            return;
        }

        String textValue = value.toString();
        if (!textValue.isEmpty()) {
            query.addCriteria(Criteria.where(field).is(value));
        }
    }

}
