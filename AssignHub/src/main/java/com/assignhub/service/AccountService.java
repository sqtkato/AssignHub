package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

@Service
public class AccountService {

	private final AccountMapper accountMapper;

	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	public List<Account> findAll(String keyword, String sort, String order) {
		return accountMapper.findAll(keyword, sort, order);
	}

	public void save(Account account) {
		accountMapper.save(account);
	}

	// 追加：ログインIDの重複チェック
	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}
}