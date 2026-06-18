package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Company;
import com.assignhub.mapper.DeletedCompanyMapper;

@Service
public class DeletedCompanyService {
	@Autowired
	private DeletedCompanyMapper deletedCompanyMapper;

	/**
	 * 論理削除済みの企業情報を全件取得する（一覧表示・検索用）。
	 */
	public List<Company> findAll(String companyName, String companyTel) {
		return deletedCompanyMapper.findAll(companyName, companyTel);
	}

	/**
	 * 選択された企業情報を取得する（CSVエクスポート用）。
	 */
	public List<Company> findByIds(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			return deletedCompanyMapper.findByIds(ids);
		}
		return null;
	}

	/**
	 * 単一復元処理。
	 */
	@Transactional
	public void restore(Integer id) {
		deletedCompanyMapper.restore(id);
	}

	/**
	 * 一括復元処理。
	 */
	@Transactional
	public void restoreBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			deletedCompanyMapper.restoreBulk(ids);
		}
	}

	/**
	 * 単一物理削除処理。
	 */
	@Transactional
	public void physicalDelete(Integer id) {
		deletedCompanyMapper.physicalDelete(id);
	}

	/**
	 * 一括物理削除処理。
	 */
	@Transactional
	public void physicalDeleteBulk(List<Integer> ids) {
		if (ids != null && !ids.isEmpty()) {
			deletedCompanyMapper.physicalDeleteBulk(ids);
		}
	}

	// =======================================================
	// Controllerからのチェック用メソッド（booleanを返す）
	// =======================================================

	/** * 単一企業に紐づく社員情報（派遣先 または パートナー所属元）が存在するか判定 
	 */
	public boolean existEmployeesByCompanyId(Integer id) {
		return deletedCompanyMapper.existEmployeesByCompanyId(id) > 0;
	}

	/** * 複数企業の中に、紐づく社員情報が存在するものが含まれているか判定 
	 */
	public boolean existEmployeesByCompanyIds(List<Integer> ids) {
		return deletedCompanyMapper.existEmployeesByCompanyIds(ids) > 0;
	}

	/** * 単一企業に紐づくアサイン履歴（現場）が存在するか判定 
	 */
	public boolean existAssignmentsByCompanyId(Integer id) {
		return deletedCompanyMapper.existAssignmentsByCompanyId(id) > 0;
	}

	/** * 複数企業の中に、紐づくアサイン履歴が存在するものが含まれているか判定 
	 */
	public boolean existAssignmentsByCompanyIds(List<Integer> ids) {
		return deletedCompanyMapper.existAssignmentsByCompanyIds(ids) > 0;
	}

	/**
	 * アサイン履歴がすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param id 復元するアサイン履歴のid
	 * @return 重複していればtrue
	 */
	// DeletedCompanyService.java 内
	public boolean isCompanyIdDuplicate(Integer excludeCompanyId) {
		// 1件ずつの id を渡す（Mapper側で自動的に "ids" に翻訳されてXMLへ届きます）
		int count = deletedCompanyMapper.countByCompanyId(excludeCompanyId);
		return count > 0;	
	}
	public boolean isCompanyIdDuplicate(List<Integer> excludeCompanyIds) {
		int count = deletedCompanyMapper.countByCompanyIds(excludeCompanyIds);
		return count > 0;
	}
	/**
	 * 復元した結果、登録上限（500件）を超えるか判定する
	 * @param restoreCount 復元しようとしている件数（単一なら1、一括ならリストのサイズ）
	 * @return 500件を超える場合はtrue
	 */
	public boolean isCompanyLimitReachedAfterRestore(int restoreCount) {
		// 1. 現在有効な（削除されていない）企業数を取得する
		// ※ 既存のCompanyService等から取得するか、独自に count を取得してください
		int currentActiveCount = deletedCompanyMapper.countActiveCompanies();

		// 2. 現在の有効数 + これから復元する件数 が 500 を超えるかチェック
		return (currentActiveCount + restoreCount) > 500;
	}

}
