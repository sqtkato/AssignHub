package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Account;
import com.assignhub.mapper.DeletedAccountMapper;

@Service
public class DeletedAccountService {

	@Autowired
    private DeletedAccountMapper deletedAccountMapper;

    /**
     * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
     */
    public List<Account> findAll(String empName,Integer permission) {
        return deletedAccountMapper.findAll(empName,permission);
    }

    /**
     * 選択されたアカウントの情報を取得する（CSVエクスポート用）。
     *
     * @param ids 画面のチェックボックスで選択されたアカウントIDのリスト
     * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    public List<Account> findByIds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
        	return deletedAccountMapper.findByIds(ids);
        }
        return null;
    }

    /**
     * 単一データをゴミ箱から復元する。
     */
    @Transactional
    public void restore(Integer id) {
        deletedAccountMapper.restore(id);
    }

    /**
     * 選択された複数のデータを一括でゴミ箱から復元する。
     * * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    @Transactional
    public void restoreBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {        
        	deletedAccountMapper.restoreBulk(ids);
        }
    }

    /**
     * 単一データをデータベースから完全に削除する。
     */
    @Transactional
    public void physicalDelete(Integer id) {
        deletedAccountMapper.physicalDelete(id);
    }

    /**
     * 選択された複数のデータをデータベースから完全に一括削除する。
     */
    @Transactional
    public void physicalDeleteBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedAccountMapper.physicalDeleteBulk(ids);
        }
    }

    // =======================================================
    // Controllerからのチェック用メソッド（booleanを返す）
    // =======================================================

    /** 単一アカウントに紐づく社員情報が存在するか判定 */
    public boolean existEmployeesByAccountId(Integer id) {
        return deletedAccountMapper.existEmployeesByAccountId(id) > 0;
    }

    /** 複数アカウントの中に、紐づく社員情報が存在するものが含まれているか判定 */
    public boolean existEmployeesByAccountIds(List<Integer> ids) {
        return deletedAccountMapper.existEmployeesByAccountIds(ids) > 0;
    }
    
    /** 復元時に重複するアカウントが存在するか判定 */
    public boolean isLoginIdDuplicate(String loginId,Integer excludeAccountId) {
		int count = deletedAccountMapper.existByLoginId(loginId,excludeAccountId);
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
		int currentActiveCount = deletedAccountMapper.existActiveCompanies();

		// 2. 現在の有効数 + これから復元する件数 が 500 を超えるかチェック
		return (currentActiveCount + restoreCount) > 500;
	}

}
