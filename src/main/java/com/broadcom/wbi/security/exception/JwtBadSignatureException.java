package com.broadcom.wbi.security.exception;


public class JwtBadSignatureException extends RuntimeException {
    public JwtBadSignatureException(String message) {
        super(message);
    }
}