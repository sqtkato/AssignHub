package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.DeletedAccount;
import com.assignhub.mapper.DeletedAccountMapper;

@Service
public class DeletedAccountService {

	@Autowired
    private DeletedAccountMapper deletedAccountMapper;

    /**
     * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
     */
    public List<DeletedAccount> getDeletedAccounts(String keyword, String sort, String order, Integer permission) {
        return deletedAccountMapper.deletedfindAll(keyword, sort, order, permission);
    }

    /**
     * 選択されたアカウントの情報を取得する（CSVエクスポート用）。
     *
     * @param ids 画面のチェックボックスで選択されたアカウントIDのリスト
     * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    public List<DeletedAccount> getExportData(List<Integer> ids) {
        if (ids != null && ids.isEmpty()) {
        	return deletedAccountMapper.deletedfindByIds(ids);
        }
        return null;
    }

    /**
     * 単一データをゴミ箱から復元する。
     */
    @Transactional
    public void restoreAccount(Integer id) {
        deletedAccountMapper.restore(id);
    }

    /**
     * 選択された複数のデータを一括でゴミ箱から復元する。
     * * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    @Transactional
    public void restoreAccountsBulk(List<Integer> ids) {
        if (ids != null && ids.isEmpty()) {        
        	deletedAccountMapper.restoreBulk(ids);
        }
    }

    /**
     * 単一データをデータベースから完全に削除する。
     */
    @Transactional
    public void physicalDeleteAccount(Integer id) {
        deletedAccountMapper.physicalDelete(id);
    }

    /**
     * 選択された複数のデータをデータベースから完全に一括削除する。
     */
    @Transactional
    public void physicalDeleteAccountsBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedAccountMapper.physicalDeleteBulk(ids);
        }
    }

    // =======================================================
    // Controllerからのチェック用メソッド（booleanを返す）
    // =======================================================

    /** 単一アカウントに紐づく社員情報が存在するか判定 */
    public boolean hasAttachedEmployees(Integer id) {
        return deletedAccountMapper.countEmployeesByAccountId(id) > 0;
    }

    /** 複数アカウントの中に、紐づく社員情報が存在するものが含まれているか判定 */
    public boolean hasAttachedEmployeesBulk(List<Integer> ids) {
        return deletedAccountMapper.countEmployeesByAccountIds(ids) > 0;
    }

    /** 単一アカウントに紐づくアサイン履歴が存在するか判定 */
    public boolean hasAttachedAssignments(Integer id) {
        return deletedAccountMapper.countAssignmentsByAccountId(id) > 0;
    }

    /** 複数アカウントの中に、紐づくアサイン履歴が存在するものが含まれているか判定 */
    public boolean hasAttachedAssignmentsBulk(List<Integer> ids) {
        return deletedAccountMapper.countAssignmentsByAccountIds(ids) > 0;
    }
}
