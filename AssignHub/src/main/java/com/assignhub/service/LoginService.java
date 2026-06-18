package com.assignhub.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

/**
 * ログイン情報に関するビジネスロジックを提供するサービスクラス。
 *
 * @version 1.00 2026/06/18
 * @author チームポケットモンスター
 */
@Service
public class LoginService {

    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
	 * コンストラクタによる依存性の注入。
	 *
	 * @param accountMapper アカウント情報マスタに対するマッパー
	 */
    public LoginService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
	 * ログインIDとパスワードを用いてアカウントの認証を行う。
	 * 
	 * @param loginId     ログインID
	 * @param rawPassword 画面から入力された生のパスワード（平文）
	 * @return 認証に成功した場合は該当するアカウントエンティティ。認証に失敗した場合はnull
	 */
    public Account authenticate(String loginId, String rawPassword) {
        Account account = accountMapper.findByLoginId(loginId);
        if (account == null) {
            return null;
        }
        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            return null;
        }
        return account;
    }
}