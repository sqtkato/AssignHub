package com.assignhub.service;

import java.util.List;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

public class AccountService {
	private final AccountMapper accountMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param employeeMapper 社員マスタに対するマッパー
	 */
	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}
	public List<Account> findAll(String keyword, Integer permission) {
		return accountMapper.findAll(keyword, permission);
	}
}
