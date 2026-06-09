package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Account;

/**
 * 企業マスタ（m_company）に対するデータベース操作を定義するマッパー。
 * @version 1.00 2026/06/01
 * @author SQT）加藤
 */
@Mapper
public  interface AccountMapper {
	
	/**
	 * 条件に一致する論理削除されていない企業情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findAll(@Param("keyword") String keyword, @Param("sort") String sort, @Param("order") String order, @Param("permission") Integer permission);


void save(Account account);
}
