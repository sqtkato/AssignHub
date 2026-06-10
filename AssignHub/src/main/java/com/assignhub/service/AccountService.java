package com.assignhub.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

@Service
public class AccountService {

	private final AccountMapper accountMapper;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
	public List<Account> findAll(String keyword, String sort, String order, Integer permission) {
		return accountMapper.findAll(keyword, sort, order, permission);

	}

	public List<Account> findByIds(List<Integer> ids){
		return accountMapper.findByIds(ids);
	}
	
	
	public void save(Account account) {

	    account.setPasswordHash(
	        passwordEncoder.encode(account.getPasswordHash())
	    );

		accountMapper.save(account);
		
	}
	// 追加：ログインIDの重複チェック
	public boolean existsByLoginId(String loginId) {
		return accountMapper.existsByLoginId(loginId);
	}
	
	/**
	 * アカウントを一件論理削除。
	 *
	 * @param id　削除対象のアカウントID
	 */
	@Transactional
	public void delete(Integer id) {
		accountMapper.delete(id);
	}


	public Account findById(Integer id) {
		return accountMapper.findById(id);
	}
	
	

	public void update(Account account) {
		accountMapper.update(account);
		
	}


		
	

	

	}
	
