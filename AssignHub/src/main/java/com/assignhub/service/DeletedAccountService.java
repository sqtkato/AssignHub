package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.DeletedAccount;
import com.assignhub.mapper.DeletedAccountMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("対象が選択されていません。");
        }
        return deletedAccountMapper.deletedfindByIds(ids);
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
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("復元対象が選択されていません。");
        }
        deletedAccountMapper.restoreBulk(ids);
    }

    /**
     * 単一データをデータベースから完全に削除する。
     */
    @Transactional
    public void physicalDeleteAccount(Integer id) {
        // 【不在条件チェック1】社員情報の紐づき確認
        int employeeCount = deletedAccountMapper.countEmployeesByAccountId(id);
        if (employeeCount > 0) {
            throw new IllegalArgumentException("社員情報に紐づいているため、物理削除できません。");
        }
        // 【不在条件チェック2】アサイン履歴の紐づき確認
        int assignmentCount = deletedAccountMapper.countAssignmentsByAccountId(id);
        if (assignmentCount > 0) {
            throw new IllegalArgumentException("アサイン履歴情報に紐づいているため、物理削除できません。");
        }

        deletedAccountMapper.physicalDelete(id);
    }

    /**
     * 選択された複数のデータをデータベースから完全に一括削除する。
     */
    @Transactional
    public void physicalDeleteAccountsBulk(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("削除対象が選択されていません。");
        }
        // 【不在条件チェック1】社員情報の紐づき確認
        int employeeCount = deletedAccountMapper.countEmployeesByAccountIds(ids);
        if (employeeCount > 0) {
            throw new IllegalArgumentException("社員情報に紐づいているデータが含まれているため、物理削除できません。");
        }
        // 【不在条件チェック2】アサイン履歴の紐づき確認
        int assignmentCount = deletedAccountMapper.countAssignmentsByAccountIds(ids);
        if (assignmentCount > 0) {
            throw new IllegalArgumentException("アサイン履歴情報に紐づいているデータが含まれているため、物理削除できません。");
        }

        deletedAccountMapper.physicalDeleteBulk(ids);
}
}
