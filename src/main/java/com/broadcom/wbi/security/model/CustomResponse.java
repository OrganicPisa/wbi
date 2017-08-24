package com.broadcom.wbi.security.model;

public class CustomResponse {
    private int code;

    private String message;

    private CustomError error;

    public CustomResponse(int code, String message, CustomError error) {
        this.code = code;
        this.message = message;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CustomError getError() {
        return error;
    }

    public void setError(CustomError error) {
        this.error = error;
    }
}
