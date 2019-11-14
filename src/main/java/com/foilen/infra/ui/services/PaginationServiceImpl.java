/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.foilen.smalltools.restapi.model.AbstractListResultWithPagination;
import com.foilen.smalltools.restapi.model.ApiPagination;
import com.foilen.smalltools.tools.JsonTools;

@Service
public class PaginationServiceImpl implements PaginationService {

    @Autowired
    private ConversionService conversionService;

    private int itemsPerPage = 100;

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    @Override
    public <T> void wrap(AbstractListResultWithPagination<T> results, Page<?> page, Class<T> apiType) {
        results.setPagination(new ApiPagination(page));
        if (page.isEmpty()) {
            results.setItems(Collections.emptyList());
        } else {
            if (conversionService.canConvert(page.getContent().get(0).getClass(), apiType)) {
                results.setItems(page.get().map(i -> conversionService.convert(i, apiType)).collect(Collectors.toList()));
            } else {
                results.setItems(page.get().map(i -> JsonTools.clone(i, apiType)).collect(Collectors.toList()));
            }
        }
    }

}
