/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui;

public class InfraUiException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InfraUiException(String message) {
        super(message);
    }

    public InfraUiException(String message, Throwable cause) {
        super(message, cause);
    }

    public InfraUiException(Throwable cause) {
        super(cause);
    }

}
