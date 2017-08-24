package com.broadcom.wbi.security.exception;

public class JwtExpirationException extends RuntimeException {
    public JwtExpirationException(String message) {
        super(message);
    }
}