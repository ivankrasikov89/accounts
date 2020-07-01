package com.seven.tech.accounts.exception;

public class AccountEntityNotFoundException extends BaseAccountException {
    public static final String CODE = "ACCOUNT_NOT_FOUND";

    public AccountEntityNotFoundException(String accountId) {
        super(CODE, accountId);
    }
}
