package com.assignhub.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

@Service
public class AccountService {

	private final AccountMapper accountMapper;

	/**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param companyMapper 企業マスタに対するマッパー
	 */
	public AccountService(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	/**
	 * 検索条件およびソート条件に合致する企業情報を全件取得する。
	 *
	 * @param keyword 検索キーワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   昇順（asc）または降順（desc）
	 * @return 企業エンティティのリスト
	 */
	public List<Account> findAll(String keyword, String sort, String order) {
		return accountMapper.findAll(keyword, sort, order);
	}

	public List<Account> findByIds(List<Integer> ids){
		return accountMapper.findByIds(ids);
	}
	
	public void save(Account account) {
		// マッパー経由でDBのINSERT/UPDATE処理を呼び出す
		accountMapper.save(account);

	}
}
