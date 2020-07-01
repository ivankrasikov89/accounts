package com.seven.tech.accounts.rest;

import com.seven.tech.accounts.BaseResult;
import com.seven.tech.accounts.json.MoneyJson;
import com.seven.tech.accounts.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(value = "/account")
public class AccountController {

	private transient AccountService accountService;

	@PostMapping("/{accountId}/increaseBalance")
	public BaseResult<MoneyJson> increaseBalance(@PathVariable("accountId") String accountId, @RequestBody MoneyJson value) {
		return new BaseResult(new MoneyJson(accountService.increaseBalance(accountId, value.getMoney())));
	}

	@PostMapping("/{accountId}/reduceBalance")
	public BaseResult<MoneyJson> reduceBalance(@PathVariable("accountId") String accountId, @RequestBody MoneyJson value) {
		return new BaseResult<>(new MoneyJson(accountService.reduceBalance(accountId, value.getMoney())));
	}

	@PostMapping("/{accountId}/transferMoney/{recipientAccountId}")
	public BaseResult<Boolean> transferMoney(@PathVariable("accountId") String accountId,
									@PathVariable("recipientAccountId") String recipientAccountId,
									@RequestBody MoneyJson value) {
		return new BaseResult<>(accountService.transferMoney(accountId, recipientAccountId, value.getMoney()));
	}

	@GetMapping("/{accountId}/getBalance")
	public BaseResult<MoneyJson> getBalance(@PathVariable("accountId") String accountId) {
		return new BaseResult(new MoneyJson(accountService.getBalance(accountId)));
	}

	@PostMapping("/createAccount")
	public BaseResult<String> createAccount() {
		return new BaseResult<>(accountService.createAccount());
	}

	@Autowired
	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}

}
