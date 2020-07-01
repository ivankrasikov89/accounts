package com.seven.tech.accounts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ConcurrentDecoratorAccountService extends AccountService {
    private static final ConcurrentHashMap<String, ReentrantLock> MAP_LOCK = new ConcurrentHashMap<>();

    private final TransactionTemplate transactionTemplate;

    public ConcurrentDecoratorAccountService(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    public Boolean transferMoney(String accountId, String recipientAccountId, BigDecimal money) {
        ReentrantLock accountLock = MAP_LOCK.computeIfAbsent(accountId, x -> new ReentrantLock());
        ReentrantLock recipientAccountLock = MAP_LOCK.computeIfAbsent(recipientAccountId, x -> new ReentrantLock());
        takeLocks(accountLock, recipientAccountLock);
        try {
            return (Boolean) transactionTemplate.execute((TransactionCallback) status ->
                    super.transferMoney(accountId, recipientAccountId, money));

        } finally {
            accountLock.unlock();
            recipientAccountLock.unlock();
        }
    }

    public BigDecimal reduceBalance(String accountId, BigDecimal money) {
        ReentrantLock reentrantLock = MAP_LOCK.computeIfAbsent(accountId, x -> new ReentrantLock());
        reentrantLock.lock();
        try {
            return (BigDecimal) transactionTemplate.execute((TransactionCallback) status ->
                    super.reduceBalance(accountId, money));
        } finally {
            reentrantLock.unlock();
        }
    }

    public BigDecimal increaseBalance(String accountId, BigDecimal money) {
        ReentrantLock reentrantLock = MAP_LOCK.computeIfAbsent(accountId, x -> new ReentrantLock());
        reentrantLock.lock();
        try {
            return (BigDecimal) transactionTemplate.execute((TransactionCallback) status ->
                    super.increaseBalance(accountId, money));
        } finally {
            reentrantLock.unlock();
        }
    }

    private void takeLocks(ReentrantLock accountLock, ReentrantLock recipientAccountLock) {
        boolean isAccountLock = false;
        boolean isRecipientAccountLockLock = false;
        while (true) {
            try {
                isAccountLock = accountLock.tryLock();
                isRecipientAccountLockLock = recipientAccountLock.tryLock();
            } finally {
                if (isAccountLock && isRecipientAccountLockLock)
                    return;
                if (isAccountLock)
                    accountLock.unlock();
                if (isRecipientAccountLockLock)
                    recipientAccountLock.unlock();
            }
            // решение для прототипа
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
