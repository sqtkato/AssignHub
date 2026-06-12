package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Account;

public interface AccountMapper {
	List<Account> findAll(@Param("keyword") String keyword, @Param("permission") Integer permission);
}
