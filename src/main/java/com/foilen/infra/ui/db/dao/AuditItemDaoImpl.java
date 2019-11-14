/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.db.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;

import com.foilen.infra.apitmp.request.RequestAuditItem;
import com.foilen.infra.ui.db.domain.audit.AuditItem;
import com.foilen.infra.ui.services.PaginationService;
import com.foilen.smalltools.tools.DateTools;
import com.google.common.base.Strings;

@Service
@Transactional
public class AuditItemDaoImpl extends SimpleJpaRepository<AuditItem, Long> implements AuditItemCustomDao {

    @Autowired
    private PaginationService paginationService;

    @Autowired
    public AuditItemDaoImpl(EntityManager em) {
        super(AuditItem.class, em);
    }

    @Override
    public Page<AuditItem> findAll(RequestAuditItem request) {

        Specification<AuditItem> specification = new Specification<AuditItem>() {
            private static final long serialVersionUID = 1L;

            private void notNullValue(Root<AuditItem> root, CriteriaBuilder criteriaBuilder, List<Predicate> restrictions, String fieldName, Enum<?> fieldValue) {
                if (fieldValue != null) {
                    restrictions.add(criteriaBuilder.equal(root.get(fieldName), fieldValue.name()));
                }
            }

            private void notNullValue(Root<AuditItem> root, CriteriaBuilder criteriaBuilder, List<Predicate> restrictions, String fieldName, String fieldValue) {
                if (!Strings.isNullOrEmpty(fieldValue)) {
                    restrictions.add(criteriaBuilder.equal(root.get(fieldName), fieldValue));
                }
            }

            @Override
            public Predicate toPredicate(Root<AuditItem> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                List<Predicate> restrictions = new ArrayList<>();

                if (!Strings.isNullOrEmpty(request.getTimestampFrom())) {
                    restrictions.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), DateTools.parseFull(request.getTimestampFrom())));
                }
                if (!Strings.isNullOrEmpty(request.getTimestampTo())) {
                    restrictions.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), DateTools.parseFull(request.getTimestampTo())));
                }

                notNullValue(root, criteriaBuilder, restrictions, "txId", request.getTxId());

                if (request.getExplicitChange() != null) {
                    restrictions.add(criteriaBuilder.equal(root.get("explicitChange"), request.getExplicitChange()));
                }

                notNullValue(root, criteriaBuilder, restrictions, "type", request.getType());
                notNullValue(root, criteriaBuilder, restrictions, "action", request.getAction());

                notNullValue(root, criteriaBuilder, restrictions, "userType", request.getUserType());
                notNullValue(root, criteriaBuilder, restrictions, "userName", request.getUserName());

                notNullValue(root, criteriaBuilder, restrictions, "resourceFirstType", request.getResourceFirstType());
                notNullValue(root, criteriaBuilder, restrictions, "resourceSecondType", request.getResourceSecondType());

                notNullValue(root, criteriaBuilder, restrictions, "linkType", request.getLinkType());
                notNullValue(root, criteriaBuilder, restrictions, "tagName", request.getTagName());

                return criteriaBuilder.and(restrictions.toArray(new Predicate[restrictions.size()]));
            }
        };

        PageRequest pageRequest = PageRequest.of(request.getPageId() - 1, paginationService.getItemsPerPage(), Direction.DESC, "timestamp", "id");
        TypedQuery<AuditItem> query = getQuery(specification, pageRequest);

        return readPage(query, AuditItem.class, pageRequest, specification);
    }

}
