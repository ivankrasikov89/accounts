package com.seven.tech.accounts.service;

import com.seven.tech.accounts.entity.AccountEntity;
import com.seven.tech.accounts.exception.AccountEntityNotFoundException;
import com.seven.tech.accounts.exception.AccountNotEnoughMoneyException;
import com.seven.tech.accounts.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;


public class AccountService {
   protected transient AccountRepository repository;

    public String createAccount() {
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID().toString());
        account.setBalance(BigDecimal.ZERO);
        repository.save(account);

        return account.getId();
    }

    @Autowired
    public void setRepository(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional( propagation = Propagation.SUPPORTS,readOnly = true )
    public BigDecimal getBalance(String accountId) {
        AccountEntity account = repository.findById(accountId).orElse(null);
        checkAccountExist(account, accountId);

        return account.getBalance();
    }

    public Boolean transferMoney(String accountId, String recipientAccountId, BigDecimal money) {
        reduceBalance(accountId, money);
        increaseBalance(recipientAccountId, money);

        return true;
    }

    public BigDecimal reduceBalance(String accountId, BigDecimal money) {
        AccountEntity account = repository.findById(accountId).orElse(null);
        checkAccountExist(account, accountId);
        checkAccountBalance(account, money);
        account.setBalance(account.getBalance().subtract(money));
        return account.getBalance();
    }

    public BigDecimal increaseBalance(String accountId, BigDecimal money) {
        AccountEntity account = repository.findById(accountId).orElse(null);
        checkAccountExist(account, accountId);
        account.setBalance(account.getBalance().add(money));
        return account.getBalance();
    }

    protected void checkAccountExist(AccountEntity account, String accountId) {
        if (account == null) {
            throw new AccountEntityNotFoundException(accountId);
        }
    }

    protected void checkAccountBalance(AccountEntity account, BigDecimal money) {
        if (account.getBalance().subtract(money).compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountNotEnoughMoneyException(account.getId());
        }
    }
}
