package com.seven.tech.accounts;

import java.io.Serializable;

public class BaseResult<T> implements Serializable {
    private boolean success = true;
    private T result;
    private String errorCode;
    private String errorMessage;

    public BaseResult(T result) {
        this.result = result;
    }
    public BaseResult(String errorCode, String errorMessage) {
        success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
