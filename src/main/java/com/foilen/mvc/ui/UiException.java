/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.mvc.ui;

public class UiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Object[] params = {};

    public UiException(String messageCode) {
        super(messageCode);
    }

    public UiException(String messageCode, Object... params) {
        super(messageCode);
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

}
