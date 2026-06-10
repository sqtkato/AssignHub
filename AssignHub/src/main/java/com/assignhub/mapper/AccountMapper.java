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
public interface AccountMapper {

	/**
	 * 条件に一致する論理削除されていない企業情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findAll(@Param("keyword") String keyword, @Param("sort") String sort, @Param("order") String order, @Param("permission") Integer permission);

	/**
	 * チェックボックスにチェックをつけたアカウント情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findByIds(@Param("ids") List<Integer> ids);

<<<<<<< HEAD
	Account findByLoginId(String loginId);
=======
	
>>>>>>> branch 'ポケットモンスター' of https://github.com/sqtkato/AssignHub.git
	
	void save(Account account);

	boolean existsByLoginId(@Param("loginId") String loginId);

	boolean existsByLoginIdUpdate(@Param("loginId") String loginId, @Param("currentAccountId") int currentAccountId);
	
	/**
	 * アカウントを一件論理削除。
	 * 
	 * @param id 削除対象のアカウントID。
	 */
	void delete(Integer id);
<<<<<<< HEAD

	// ===== インポート機能用に追加 =====

	/** アカウントIDで1件取得（存在チェック・更新前確認用）。 */
	Account findById(@Param("accountId") Integer accountId);

	/** アカウントを更新する（ログインID・パスワード・権限を上書き）。 */
=======
	
	Account findById(Integer id);
	
	Account findByLoginId(String loginId);

>>>>>>> branch 'ポケットモンスター' of https://github.com/sqtkato/AssignHub.git
	void update(Account account);

	
}
