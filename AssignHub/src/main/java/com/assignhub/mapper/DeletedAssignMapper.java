package com.assignhub.mapper;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Assignment;

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
	List<Assignment> findAll(
			@Param("empName") String empName,
			@Param("assignName") String assignName,
			@Param("companyName") String companyName,
			@Param("contractStartDate") String contractStartDate,
			@Param("contractEndDate") String contractEndDate);

	List<Assignment> findAllByIds(@Param("ids") List<Integer> ids);

	/**
	 * 単一復元（対象データの delete_flg を 0 に更新する）
	 * * @param id アサインID
	 * @return 影響を受けた行数（成功時は1、排他エラー時は0）
	 */
	int restore(@Param("id") Integer id);

	int countDuplicate(
			@Param("assignmentId") Integer assignmentId,
			@Param("empId") Integer empId,
			@Param("companyId") Integer companyId,
			@Param("contractStartDate") LocalDate contractStartDate,
			@Param("contractEndDate") LocalDate contractEndDate);

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


	// DeletedAssignMapper.java の中に追記するコード

	/**
	 * 現在有効な（delete_flg = 0 の）アサイン履歴件数を取得する
	 */
	int countActiveAssigns();
	
	/**
	 * 指定されたアサインIDに紐づく企業数(派遣先)をカウントする（単一復元の生存チェック用）。
	 */
	int existCompanyDispatchsByAssignId(@Param("id") Integer id);

	/**
	 * 指定されたアサインIDのいずれかに紐づく企業数(派遣先)をカウントする（一括復元の生存条件チェック用）。
	 */
	int existCompanyDispatchsByAssignIds(@Param("ids") List<Integer> ids);

	/**
	 * 指定されたアサインIDに紐づく社員情報をカウントする（単一復元の生存条件チェック用）。
	 */
	int existEmployeeByAssignId(@Param("id") Integer id);

	/**
	 * 指定されたアサインIDのいずれかに紐づく社員情報をカウントする（一括復元の生存条件チェック用）。
	 */
	int existEmployeeByAssignIds(@Param("ids") List<Integer> ids);

	/**
	
	/**
	 * 指定されたIDのレコードがすでに存在しているか確認（重複チェック）。
	 */
	int isDuplicate(@Param("ids") Integer ids);

}
