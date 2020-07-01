package com.seven.tech.accounts.exception;

public abstract class BaseAccountException extends RuntimeException {
    private String code;

    protected BaseAccountException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
