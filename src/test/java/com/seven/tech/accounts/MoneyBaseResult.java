package com.seven.tech.accounts;

import com.seven.tech.accounts.json.MoneyJson;



public class MoneyBaseResult extends BaseResult<MoneyJson> {
    public MoneyBaseResult(MoneyJson result) {
        super(result);
    }

    public MoneyBaseResult(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
