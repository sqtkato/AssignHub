package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Account;

@Mapper
public interface DeletedAccountMapper {
	
	/**
	 * 条件に一致する論理削除済み企業情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findAll(@Param("empName") String empName, @Param("permission") Integer permission);

	/**
	 * チェックボックスにチェックをつけたアカウント情報を全件取得する。
	 *
	 * @param keyword 検索ワード（企業名の部分一致）
	 * @param sort    ソート対象のカラム名
	 * @param order   ソート順（asc または desc）
	 * @return アカウントエンティティのリスト
	 */
	List<Account> findByIds(@Param("ids") List<Integer> ids);
	
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
	int existEmployeesByAccountId(@Param("id") Integer id);
	
	/**
	 * 指定されたアカウントIDリストのいずれかに紐づく社員数をカウントする（一括物理削除の不在条件チェック用）。
	 */
	int existEmployeesByAccountIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 復元時に重複するアカウントが存在するか判定
	 */
	int countByLoginId(@Param("excludeAccountId") Integer excludeAccountId);

	int countByLoginIds(@Param("excludeAccountIds") List<Integer> excludeAccountIds);
	
	/**
	 * 現在有効な（削除されていない）企業総数をカウントする。
	 * （500件登録上限チェック用）
	 */
	int countActiveAccounts();
}
