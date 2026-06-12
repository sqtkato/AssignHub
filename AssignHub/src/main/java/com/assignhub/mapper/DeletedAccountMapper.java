package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Account;

@Mapper
public interface DeletedAccountMapper {
	/**
	 * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
	 *
	 * @param keyword    検索ワード（ログインIDや社員名などの部分一致）
	 * @param sort       ソート対象のカラム名
	 * @param order      ソート順（asc または desc）
	 * @param permission 権限による絞り込み（0:一般、1:管理者）
	 * @return 削除済みアカウントエンティティのリスト
	 */
	List<Account> deletedfindAll(
			@Param("keyword") String keyword, 
			@Param("sort") String sort, 
			@Param("order") String order, 
			@Param("permission") Integer permission);
	
	/**
	 * チェックボックスにチェックをつけた複数のアカウント情報を取得する（エクスポート処理用）。
	 *
	 * @param ids 選択されたアカウントIDのリスト
	 * @return 対象アカウントエンティティのリスト
	 */
	List<Account> deletedfindByIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 単一復元（対象データの delete_flg を 0 に更新する）
	 * * @param id アカウントID
	 * @return 影響を受けた行数（成功時は1、排他エラー時は0）
	 */
	int restore(@Param("id") Integer id);
	
	/**
	 * 一括復元（選択された複数データの delete_flg を 0 に更新する）
	 * * @param ids アカウントIDのリスト
	 * @return 影響を受けた行数（実際に更新された件数）
	 */
	int restoreBulk(@Param("ids") List<Integer> ids);
	
	/**
	 * 単一物理削除（対象データをデータベースから完全に消去する）
	 * * @param id アカウントI
	 * @return 影響を受けた行数（成功時は1、失敗時は0）
	 */
	int physicalDelete(@Param("id") Integer id);
	
	/**
	 * 一括物理削除（選択された複数データをデータベースから完全に消去する）
	 * * @param ids アカウントIDのリスト
	 * @return 影響を受けた行数（実際に削除された件数）
	 */
	int physicalDeleteBulk(@Param("ids") List<Integer> ids);
	
	/**
	 * 指定されたアカウントIDに紐づく社員数をカウントする（単一物理削除の不在条件チェック用）。
	 */
	int countEmployeesByAccountId(@Param("id") Integer id);
	
	/**
	 * 指定されたアカウントIDリストのいずれかに紐づく社員数をカウントする（一括物理削除の不在条件チェック用）。
	 */
	int countEmployeesByAccountIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 指定されたアカウントIDに紐づくアサイン履歴数をカウントする（単一物理削除の不在条件チェック用）。
	 */
	int countAssignmentsByAccountId(@Param("id") Integer id);

	/**
	 * 指定されたアカウントIDリストのいずれかに紐づくアサイン履歴数をカウントする（一括物理削除の不在条件チェック用）。
	 */
	int countAssignmentsByAccountIds(@Param("ids") List<Integer> ids);

}
