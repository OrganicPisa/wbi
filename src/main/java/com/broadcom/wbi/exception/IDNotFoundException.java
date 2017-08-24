package com.broadcom.wbi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such ID")
public class IDNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IDNotFoundException(Integer key, String table) {
        super(Integer.toString(key) + " is not available in " + table + " table");
    }

    public IDNotFoundException(Integer key) {
        super(Integer.toString(key) + " is not available in database");
    }
}
