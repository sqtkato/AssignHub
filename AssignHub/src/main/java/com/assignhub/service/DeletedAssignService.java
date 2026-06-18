package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Assignment;
import com.assignhub.mapper.DeletedAssignMapper;

@Service
public class DeletedAssignService {
	@Autowired
	private DeletedAssignMapper deletedAssignMapper;

	/**
	 * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
	 */
	public List<Assignment> findAll(String empName, String assignName, String companyName,
			String contractStartDate, String contractEndDate) {
		return deletedAssignMapper.findAll(empName, assignName, companyName, contractStartDate,
				contractEndDate);
	}

	/**
	 * 選択されたアカウントの情報を取得する（CSVエクスポート用）。
	 *
	 * @param ids 画面のチェックボックスで選択されたアカウントIDのリスト
	 * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
	 */
	public List<Assignment> findAllByIds(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			return deletedAssignMapper.findAllByIds(ids);
		}
		return null;
	}

	/**
	 * 単一データをゴミ箱から復元する。
	 */
	@Transactional
	public void restore(Integer id) {
		deletedAssignMapper.restore(id);
	}

	/**
	 * 選択された複数のデータを一括でゴミ箱から復元する。
	 * * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
	 */
	@Transactional
	public void restoreBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			deletedAssignMapper.restoreBulk(ids);
		}
	}

	/**
	 * 単一データをデータベースから完全に削除する。
	 */
	@Transactional
	public void physicalDelete(Integer id) {
		deletedAssignMapper.physicalDelete(id);
	}

	/**
	 * 選択された複数のデータをデータベースから完全に一括削除する。
	 */
	@Transactional
	public void physicalDeleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			deletedAssignMapper.physicalDeleteBulk(ids);
		}
	}

	// =======================================================
	// Controllerからのチェック用メソッド（booleanを返す）
	// =======================================================

	//    単一復元の不在条件
	public boolean existCompanyDispatchsByAssignId(Integer id) {
		return deletedAssignMapper.existCompanyDispatchsByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean existCompanyDispatchsByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.existCompanyDispatchsByAssignIds(ids) > 0;
	}

	//    単一復元の生存条件
	public boolean existEmployeeByAssignId(Integer id) {
		return deletedAssignMapper.existEmployeeByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean existEmployeeByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.existEmployeeByAssignIds(ids) > 0;
	}

	/**
	 * アサイン履歴情報の企業ID社員ID契約開始日契約終了日がすでに登録されているか（重複しているか）を判定する。
	 * @return 重複していればtrue
	 */
	public boolean isAssignIdDuplicate(Integer excludeAssignId) {
		int count = deletedAssignMapper.countByAssignId(excludeAssignId);
		return count > 0;
	}
	/**
	* 復元した結果、登録上限（500件）を超えるか判定する
	* @param restoreCount 復元しようとしている件数（単一なら1、一括ならリストのサイズ）
	* @return 500件を超える場合はtrue
	*/
	public boolean isAssignIdsDuplicate(List<Integer> excludeAssignIds) {
		int count = deletedAssignMapper.countByAssignIds(excludeAssignIds);
		return count > 0;
	}

	public boolean existsDuplicate(Assignment assignment) {
		int count = deletedAssignMapper.countDuplicate(
				assignment.getAssignmentId(),
				assignment.getEmpId(),
				assignment.getCompanyId(),
				assignment.getContractStartDate(),
				assignment.getContractEndDate());
		return count > 0;
	}

	public boolean isAssginLimitReachedAfterRestore(int restoreCount) {
		// 1. 現在有効な（削除されていない）企業数を取得する
		// ※ 既存のCompanyService等から取得するか、独自に count を取得してください
		int currentActiveCount = deletedAssignMapper.countActiveAssigns();

		// 2. 現在の有効数 + これから復元する件数 が 500 を超えるかチェック
		return (currentActiveCount + restoreCount) > 500;
	}

}
