package com.seven.tech.accounts;

import com.seven.tech.accounts.exception.AccountEntityNotFoundException;
import com.seven.tech.accounts.exception.AccountNotEnoughMoneyException;
import com.seven.tech.accounts.json.MoneyJson;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountsApplicationTests {

	private static final int EXPECTED_LENGTH_ID = 36;
	private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(20000).setScale(2, RoundingMode.HALF_EVEN);
	private static final BigDecimal TRANSFER_VALUE = BigDecimal.TEN.setScale(2, RoundingMode.HALF_EVEN);
	private static final int COUNT_TREADS = 40;

	@LocalServerPort
	private int port;

	@Test
	void contextLoads() {}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createAccountShouldReturnIdAccount() {
		BaseResult<String> response = createAccount();

		assertTrue(response.isSuccess());
		assertNotNull(response.getResult());
		assertThat(response.getResult().length()).isEqualTo(EXPECTED_LENGTH_ID);
	}

	@Test
	void getBalanceShouldReturnZeroWhenNewAccount() {
		String idAccount = createAccount().getResult();

		BaseResult<MoneyJson> response = getBalance(idAccount);

		assertTrue(response.isSuccess());
		assertThat(response.getResult().getMoney()).isEqualTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void getBalanceShouldReturnErrorWhenAccountNotFound() {
		BaseResult<MoneyJson> response = getBalance(UUID.randomUUID().toString());

		assertFalse(response.isSuccess());
		assertThat(response.getErrorCode()).isEqualTo(AccountEntityNotFoundException.CODE);
	}

	@Test
	void increaseBalanceShouldReturnCorrectBalanceWhenAccountEmpty() {
		String accountId = createAccount().getResult();

		BaseResult<MoneyJson> response = increaseBalance(accountId, new MoneyJson(TRANSFER_VALUE));

		assertTrue(response.isSuccess());
		assertThat(response.getResult().getMoney()).isEqualTo(TRANSFER_VALUE);
		assertThat(response.getResult().getMoney()).isEqualTo(getBalance(accountId).getResult().getMoney());
	}

	@Test
	void increaseBalanceShouldReturnCorrectBalanceWhenBalanceAccountNotEmpty() {
		String accountId = createAccount().getResult();
		increaseBalance(accountId, new MoneyJson(INITIAL_BALANCE));

		BaseResult<MoneyJson> response = increaseBalance(accountId, new MoneyJson(TRANSFER_VALUE));

		assertTrue(response.isSuccess());
		assertThat(response.getResult().getMoney()).isEqualTo(INITIAL_BALANCE.add(TRANSFER_VALUE));
		assertThat(response.getResult().getMoney()).isEqualTo(getBalance(accountId).getResult().getMoney());
	}

	@Test
	void reduceBalanceShouldReturnErrorWhenMoneyNotFound() {
		String accountId = createAccount().getResult();

		BaseResult<MoneyJson> response = reduceBalance(accountId, new MoneyJson(TRANSFER_VALUE));

		assertFalse(response.isSuccess());
		assertThat(response.getErrorCode()).isEqualTo(AccountNotEnoughMoneyException.CODE);
	}

	@Test
	void reduceBalanceShouldReturnBalanceWhenBalanceAccountNotEmpty() {
		String accountId = createAccount().getResult();
		increaseBalance(accountId, new MoneyJson(INITIAL_BALANCE));

		BaseResult<MoneyJson> response = reduceBalance(accountId, new MoneyJson(TRANSFER_VALUE));

		assertTrue(response.isSuccess());
		assertThat(response.getResult().getMoney()).isEqualTo(INITIAL_BALANCE.subtract(TRANSFER_VALUE));
		assertThat(response.getResult().getMoney()).isEqualTo(getBalance(accountId).getResult().getMoney());
	}

	@Test
	void transferMoneyShouldCorrectChangeBalance() {
		String accountId = createAccount().getResult();
		increaseBalance(accountId, new MoneyJson(INITIAL_BALANCE));
		String recipientAccountId = createAccount().getResult();

		BaseResult<Boolean> response = transferMoney(accountId, recipientAccountId, new MoneyJson(TRANSFER_VALUE));

		assertTrue(response.isSuccess());
		assertTrue(response.getResult());
	}

	@Test
	void transferMoneyShouldReturnErrorWhenMoneyNotFound() {
		String accountId = createAccount().getResult();
		String recipientAccountId = createAccount().getResult();

		BaseResult<Boolean> response = transferMoney(accountId, recipientAccountId, new MoneyJson(TRANSFER_VALUE));

		assertFalse(response.isSuccess());
		assertThat(response.getErrorCode()).isEqualTo(AccountNotEnoughMoneyException.CODE);
	}

	@Execution(ExecutionMode.CONCURRENT)
	@TestFactory
	Collection<DynamicTest> concurrentTransferShouldNotToLoseMoney() {
		String accountId = createAccount().getResult();
		increaseBalance(accountId, new MoneyJson(INITIAL_BALANCE));
		String recipientAccountId = createAccount().getResult();
		increaseBalance(recipientAccountId, new MoneyJson(INITIAL_BALANCE));
		CountDownLatch countDownLatch = new CountDownLatch(COUNT_TREADS);


		List<DynamicTest> dynamicTests = IntStream.iterate(0, n -> n + 1).limit(COUNT_TREADS).
				mapToObj(n -> DynamicTest.dynamicTest("transfer " + n, () -> {
					assertTrue(transferMoney(accountId, recipientAccountId, new MoneyJson(TRANSFER_VALUE)).isSuccess());
					assertTrue(transferMoney(recipientAccountId, accountId, new MoneyJson(TRANSFER_VALUE)).isSuccess());
					countDownLatch.countDown();
		})).collect(Collectors.toList());
		dynamicTests.add(dynamicTest("final transfer test", () -> {
			countDownLatch.await(10, TimeUnit.SECONDS);
			assertEquals(getBalance(accountId).getResult().getMoney(), INITIAL_BALANCE);
			assertEquals(getBalance(recipientAccountId).getResult().getMoney(), INITIAL_BALANCE);
		}));

		return dynamicTests;
	}

	@Execution(ExecutionMode.CONCURRENT)
	@TestFactory
	Collection<DynamicTest> concurrentIncreaseShouldNotToLoseMoney() {
		String accountId = createAccount().getResult();
		increaseBalance(accountId, new MoneyJson(INITIAL_BALANCE));
		CountDownLatch countDownLatch = new CountDownLatch(COUNT_TREADS);

		List<DynamicTest> dynamicTests = IntStream.iterate(0, n -> n + 1).limit(COUNT_TREADS).
				mapToObj(n -> DynamicTest.dynamicTest("increase " + n, () -> {
					assertTrue(increaseBalance(accountId, new MoneyJson(TRANSFER_VALUE)).isSuccess());
					countDownLatch.countDown();
				})).collect(Collectors.toList());
		dynamicTests.add(dynamicTest("final increase test", () -> {
			countDownLatch.await(10, TimeUnit.SECONDS);
			assertEquals(getBalance(accountId).getResult().getMoney(),
					INITIAL_BALANCE.add(TRANSFER_VALUE.multiply(BigDecimal.valueOf(COUNT_TREADS))));
		}));

		return dynamicTests;
	}

	@Execution(ExecutionMode.CONCURRENT)
	@TestFactory
	Collection<DynamicTest> concurrentReduceShouldNotToLoseMoney() {
		String accountId = createAccount().getResult();
		BigDecimal initialBalance = BigDecimal.valueOf(200000);
		increaseBalance(accountId, new MoneyJson(initialBalance));
		CountDownLatch countDownLatch = new CountDownLatch(COUNT_TREADS);


		List<DynamicTest> dynamicTests = IntStream.iterate(0, n -> n + 1).limit(COUNT_TREADS).
				mapToObj(n -> DynamicTest.dynamicTest("reduce " + n, () -> {
					assertTrue(reduceBalance(accountId, new MoneyJson(TRANSFER_VALUE)).isSuccess());
					countDownLatch.countDown();
				})).collect(Collectors.toList());
		dynamicTests.add(dynamicTest("final reduce test", () -> {
			countDownLatch.await(10, TimeUnit.SECONDS);
			assertEquals(getBalance(accountId).getResult().getMoney(),
					initialBalance.subtract(TRANSFER_VALUE.multiply(BigDecimal.valueOf(COUNT_TREADS))));
		}));

		return dynamicTests;
	}



	private BaseResult<MoneyJson> getBalance(String accountId) {
		return restTemplate.getForObject(getBasicUrl() + accountId + "/getBalance", MoneyBaseResult.class);
	}

	private BaseResult<MoneyJson> increaseBalance(String accountId, MoneyJson moneyJson) {
		return restTemplate.postForObject(getBasicUrl() + accountId + "/increaseBalance", moneyJson, MoneyBaseResult.class);
	}

	private BaseResult<MoneyJson> reduceBalance(String accountId, MoneyJson moneyJson) {
		return restTemplate.postForObject(getBasicUrl() + accountId + "/reduceBalance", moneyJson, MoneyBaseResult.class);
	}

	private BaseResult<Boolean> transferMoney(String accountId, String recipientAccountId, MoneyJson moneyJson) {
		return restTemplate.postForObject(getBasicUrl() + accountId + "/transferMoney/" + recipientAccountId,
				moneyJson, BaseResult.class);
	}

	private BaseResult<String> createAccount() {
		return restTemplate.postForObject(getBasicUrl() + "/createAccount", null, BaseResult.class);
	}

	private String getBasicUrl() {
		return "http://localhost:" + port + "/account/";
	}

}
