package com.seven.tech.accounts.exception;

public class AccountNotEnoughMoneyException extends BaseAccountException {
    public static final String CODE = "ACCOUNT_NOT_ENOUGH_MONEY";

    public AccountNotEnoughMoneyException(String accountId) {
        super(CODE, accountId);
    }
}
