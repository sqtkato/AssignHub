package com.assignhub.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.assignhub.entity.Account;
import com.assignhub.mapper.AccountMapper;

@Service
public class LoginService {

    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

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