/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.api.model.audit.AuditItemWithPagination;
import com.foilen.infra.api.request.RequestAuditItem;
import com.foilen.infra.plugin.v1.core.service.TranslationService;
import com.foilen.infra.ui.repositories.AuditItemRepository;
import com.foilen.infra.ui.repositories.documents.AuditItem;
import com.foilen.smalltools.restapi.model.ApiError;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.DateTools;

@Service
@Transactional
public class ApiAuditManagementServiceImpl extends AbstractApiService implements ApiAuditManagementService {

    @Autowired
    private AuditItemRepository auditItemDao;
    @Autowired
    private PaginationService paginationService;
    @Autowired
    private TranslationService translationService;

    @Override
    public AuditItemWithPagination auditFindAll(String userId, RequestAuditItem request) {

        AuditItemWithPagination results = new AuditItemWithPagination();

        // Validate fields
        if (request.getPageId() < 1) {
            CollectionsTools.getOrCreateEmptyArrayList(results.getValidationErrorsByField(), "pageId", String.class).add(translationService.translate("error.pageStart1"));
        }
        Date timestampFrom = null;
        Date timestampTo = null;
        if (request.getTimestampFrom() != null) {
            try {
                timestampFrom = DateTools.parseFull(request.getTimestampFrom());
            } catch (Exception e) {
                CollectionsTools.getOrCreateEmptyArrayList(results.getValidationErrorsByField(), "timestampFrom", String.class).add(translationService.translate("error.wrongTimestampFormat"));
            }
        }
        if (request.getTimestampTo() != null) {
            try {
                timestampTo = DateTools.parseFull(request.getTimestampTo());
            } catch (Exception e) {
                CollectionsTools.getOrCreateEmptyArrayList(results.getValidationErrorsByField(), "timestampTo", String.class).add(translationService.translate("error.wrongTimestampFormat"));
            }
        }
        if (timestampFrom != null && timestampTo != null) {
            if (!DateTools.isAfter(timestampTo, timestampFrom)) {
                CollectionsTools.getOrCreateEmptyArrayList(results.getValidationErrorsByField(), "timestampTo", String.class).add(translationService.translate("error.mustBeAfter"));
            }
        }

        // Validate entitlement
        if (!entitlementService.isAdmin(userId)) {
            results.setError(new ApiError(translationService.translate("error.notAdmin")));
        }

        if (!results.isSuccess()) {
            return results;
        }

        Page<AuditItem> items = auditItemDao.findAll(request);
        paginationService.wrap(results, items, com.foilen.infra.api.model.audit.AuditItem.class);
        return results;
    }

}
