package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Account;

/**
 * アカウントマスタ（m_account）に対するデータベース操作を定義するマッパー。
 * @version 1.00 2026/06/18
 * @author チームポケットモンスター
 */
@Mapper
public interface AccountMapper {

	/**
	 * 条件に一致する論理削除されていない企業情報を全件取得する。
	 *
	 * @param keyword 検索ワード（社員名の部分一致）
	 * @param permission 権限検索（選択した権限）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findAll(@Param("keyword") String keyword, @Param("permission") Integer permission);

	/**
	 * チェックボックスにチェックをつけたアカウント情報を取得する。
	 *
	 * @param accountId チェックをつけたアカウントID
	 * @return アカウントエンティティ
	 */
	/** アカウントIDで1件取得（存在チェック・更新前確認用）。 */
	Account findById(@Param("accountId") Integer accountId);

	/**
	 * チェックボックスにチェックをつけたアカウント情報を全件取得する。
	 * 
	 * @param ids チェックをつけたアカウントIDのリスト
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findByIds(@Param("ids") List<Integer> ids);
	
	/**
	 * ログインIDでアカウント情報を取得する。
	 * 
	 * @return アカウントエンティティ
	 */
	Account findByLoginId(String loginId);
	
	/**
	 * アカウントを新規登録する。
	 * 
	 * @param account アカウントエンティティ
	 */
	void insert(Account account);

	/**
	 * アカウントを更新する。
	 * 
	 * @param account アカウントエンティティ
	 */
	void update(Account account);

	/**
	 * アカウントを論理削除する。
	 * 
	 * @param id 削除対象のアカウントID
	 */
	void delete(Integer id);

	/**
	 * アカウントを一括で論理削除する。
	 * 
	 * @param ids 削除対象のアカウントIDのリスト
	 */
	void deleteBulk(@Param("ids") List<Integer> ids);

	/**
	 * ログインIDの重複チェック。
	 * 
	 * @param id				重複チェック対象のログインID
	 * @param excludeAccountId	変更前のログインID（新規登録時はnull）
	 * @return
	 */
	int countByLoginId(@Param("loginId") String id, @Param("excludeAccountId") Integer excludeAccountId);

	/**
	 * 登録済みアカウント数を調べる。
	 * 
	 * @return 論理削除されていないアカウント数
	 */
	int countAll();
	
	List<Account> findLoginId();
}
