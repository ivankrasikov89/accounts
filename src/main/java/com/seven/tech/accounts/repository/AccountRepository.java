package com.seven.tech.accounts.repository;

import com.seven.tech.accounts.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {}
