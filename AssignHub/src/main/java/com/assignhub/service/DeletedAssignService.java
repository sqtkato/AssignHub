package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Assignment;
import com.assignhub.mapper.DeletedAssignMapper;

public class DeletedAssignService {
	@Autowired
	private DeletedAssignMapper deletedAssignMapper;

	/**
	 * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
	 */
	public List<Assignment> findAll(String txtEmpName, String txtAssignName, String txtCompanyName,
			String txtContractStartDate, String txtContractEndDate) {
		return deletedAssignMapper.findAll(txtEmpName, txtAssignName, txtCompanyName, txtContractStartDate,
				txtContractEndDate);
	}

	/**
	 * 選択されたアカウントの情報を取得する（CSVエクスポート用）。
	 *
	 * @param ids 画面のチェックボックスで選択されたアカウントIDのリスト
	 * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
	 */
	public List<Assignment> findAllByIds(List<Integer> ids) {
		if (ids != null && ids.isEmpty()) {
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
	public boolean countcompanysdispatchsByAssignId(Integer id) {
		return deletedAssignMapper.countcompanysdispatchsByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean countcompanysdispatchsByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.countcompanysdispatchsByAssignIds(ids) > 0;
	}

	//    単一復元の生存条件
	public boolean countcompanyspartnerByAssignId(Integer id) {
		return deletedAssignMapper.countcompanyspartnerByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean countcompanyspartnerByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.countcompanyspartnerByAssignIds(ids) > 0;
	}

	//    単一復元の生存条件
	public boolean countEmployeesproperByAssignId(Integer id) {
		return deletedAssignMapper.countEmployeesproperByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean countEmployeesproperByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.countEmployeesproperByAssignIds(ids) > 0;
	}

	//    単一復元の生存条件
	public boolean countEmployeespartnerByAssignId(Integer id) {
		return deletedAssignMapper.countEmployeespartnerByAssignId(id) > 0;
	}

	//    一括復元の生存条件
	public boolean countEmployeespartnersByAssignIds(List<Integer> ids) {
		return deletedAssignMapper.countEmployeespartnersByAssignIds(ids) > 0;
	}
}
