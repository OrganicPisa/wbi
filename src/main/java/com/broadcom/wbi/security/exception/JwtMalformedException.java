package com.broadcom.wbi.security.exception;

public class JwtMalformedException extends RuntimeException {
    public JwtMalformedException(String message) {
        super(message);
    }
}
