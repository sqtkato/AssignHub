package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.DeletedAssign;

@Mapper
public interface DeletedAssignMapper {
	/**
	 * 論理削除済みのアサイン履歴情報を全件取得する（一覧表示・検索用）。
	 *
	 * @param txtEmpName 社員名の検索キーワード
	 * 
     * @param txtCompanyName 企業名の検索キーワード
     * @return アサイン情報のリスト
	 */
	List<DeletedAssign>findAll(
			@Param("txtEmpName") String txtEmpName, 
            @Param("txtAssignName") String txtAssignName,
            @Param("txtCompanyName") String txtCompanyName,
            @Param("txtContractStartDate") String txtContractStartDate,
            @Param("txtContractEndDate") String txtContractEndDate);
	
List<DeletedAssign> findallByIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 単一復元（対象データの delete_flg を 0 に更新する）
	 * * @param id アサインID
	 * @return 影響を受けた行数（成功時は1、排他エラー時は0）
	 */
	int restore(@Param("id") Integer id);
	
	/**
	 * 一括復元（選択された複数データの delete_flg を 0 に更新する）
	 * * @param ids アサインIDのリスト
	 * @return 影響を受けた行数（実際に更新された件数）
	 */
	int restoreBulk(@Param("ids") List<Integer> ids);
	
	/**
	 * 単一物理削除（対象データをデータベースから完全に消去する）
	 * * @param id アサインID
	 * @return 影響を受けた行数（成功時は1、失敗時は0）
	 */
	int physicalDelete(@Param("id") Integer id);
	
	/**
	 * 一括物理削除（選択された複数データをデータベースから完全に消去する）
	 * * @param ids アサインIDのリスト
	 * @return 影響を受けた行数（実際に削除された件数）
	 */
	int physicalDeleteBulk(@Param("ids") List<Integer> ids);
	
	/**
	 * 指定されたアサインIDに紐づく企業数(派遣先)をカウントする（単一復元の不在条件チェック用）。
	 */
	int countcompanysdispatchsByAssignId(@Param("id") Integer id);
	/**
	 * 指定されたアサインIDのいずれかに紐づく企業数(派遣先)をカウントする（一括復元の不在条件チェック用）。
	 */
	int countcompanysdispatchsByAssignIds(@Param("ids") List<Integer> ids);
	/**
	 * 指定されたアサインIDに紐づく企業数(パートナー所属）をカウントする（単一復元の不在条件チェック用）。
	 */
	int countcompanyspartnerByAssignId(@Param("id") Integer id);
	/**
	 * 指定されたアサインIDのいずれかに紐づく企業数(パートナー所属）をカウントする（単一復元の不在条件チェック用）。
	 */
	int countcompanyspartnerByAssignIds(@Param("ids") List<Integer> ids);
	/**
	 * 指定されたアサインIDに紐づく社員情報(プロパー)をカウントする（一括復元の不在条件チェック用）。
	 */
	int countEmployeesByAssignIds(@Param("ids") List<Integer> ids);
	
	/**
	 * 指定されたアカウントIDに紐づくアサイン履歴数をカウントする（一括復元の不在条件チェック用）。
	 */
	int countAssignmentsByAccountId(@Param("id") Integer id);

	/**
	 * 指定されたアカウントIDリストのいずれかに紐づくアサイン履歴数をカウントする（一括復元の不在条件チェック用）。
	 */
	int countAssignmentsByAccountIds(@Param("ids") List<Integer> ids);


}



