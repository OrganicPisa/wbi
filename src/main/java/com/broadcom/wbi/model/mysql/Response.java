package com.broadcom.wbi.model.mysql;

public class Response {
    private int code;

    private String message;

    private CustomError error;

    public Response(int code, String message, CustomError error) {
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
