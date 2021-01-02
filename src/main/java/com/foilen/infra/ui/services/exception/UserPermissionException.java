/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services.exception;

public class UserPermissionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserPermissionException(String message) {
        super(message);
    }

    public UserPermissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserPermissionException(Throwable cause) {
        super(cause);
    }

}
