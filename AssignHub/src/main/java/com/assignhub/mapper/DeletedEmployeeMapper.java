package com.assignhub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.assignhub.entity.Employee;

@Mapper
public interface DeletedEmployeeMapper {
	/**
	 * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
	 *
	 * @param keyword    検索ワード（ログインIDや社員名などの部分一致）
	 * @param sort       ソート対象のカラム名
	 * @param order      ソート順（asc または desc）
	 * @param permission 権限による絞り込み（0:一般、1:管理者）
	 * @return 削除済みアカウントエンティティのリスト
	 */
	List<Employee> findAll(@Param("empName") String empName, @Param("empAssignCompany") String empAssignCompany,
			@Param("empEngineerType") String empEngineerType, @Param("empCompany") String empCompany);

	/**
	 * チェックボックスにチェックをつけた複数のアカウント情報を取得する（エクスポート処理用）。
	 *
	 * @param ids 選択されたアカウントIDのリスト
	 * @return 対象アカウントエンティティのリスト
	 */
	List<Employee> findByIds(@Param("ids") List<Integer> ids);

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
	 * * @param id アカウントID
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
	 * 指定された社員IDに紐づく社員数をカウントする（単一物理削除の不在条件チェック用）。
	 */
	int existAccountsByEmpolyeeId(@Param("id") Integer id);

	/**
	 * 指定された社員IDリストのいずれかに紐づくアカウントをカウントする（一括物理削除の不在条件チェック用）。
	 */
	int existAccountsByEmpolyeeIds(@Param("ids") List<Integer> ids);

	/**
	 * 指定された社員IDに紐づく企業情報数をカウントする（単一物理削除の不在条件チェック用）。
	 */
	int existCompaniesByEmpolyeeId(@Param("id") Integer id);

	/**
	 * 指定された社員IDリストのいずれかに紐づく企業情報数をカウントする（一括物理削除の不在条件チェック用）。
	 */
	int existCompaniesByEmpolyeeIds(@Param("ids") List<Integer> ids);

	/**
	 * 指定された社員IDに紐づくアサイン履歴数をカウントする（単一物理削除の不在条件チェック用）。
	 */
	int existAssignmentsByEmpolyeeId(@Param("id") Integer id);

	/**
	 * 指定された社員IDリストのいずれかに紐づくアサイン履歴数をカウントする（一括物理削除の不在条件チェック用）。
	 */
	int existAssignmentsByEmpolyeeIds(@Param("ids") List<Integer> ids);

}
