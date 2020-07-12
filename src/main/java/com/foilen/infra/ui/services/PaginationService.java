/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;
import com.foilen.smalltools.restapi.model.AbstractSingleResult;

public interface PaginationService {

    int getItemsPerPage();

    <T> void wrap(AbstractListResultWithPagination<T> results, Page<?> page, Class<T> apiType);

    <T> void wrap(AbstractListResultWithPagination<T> results, Page<T> page);

    <T> void wrap(AbstractSingleResult<T> result, Optional<?> item, Class<T> apiType);

}
