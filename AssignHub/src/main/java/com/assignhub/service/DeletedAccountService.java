package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Account;
import com.assignhub.mapper.DeletedAccountMapper;

/**
 * 論理削除されたアカウント情報の管理、復元、物理削除、エクスポートを処理するサービス。
 * @author C3S) 野本
 */
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
     * @return 該当するアカウントエンティティ（存在しない場合はnull）
     */
    public List<Account> findByIds(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
        	return deletedAccountMapper.findByIds(ids);
        }
        return null;
    }

    /**
     * 論理削除済みアカウントを一件復元する。
     * 
     * @param id 復元対象のアカウントID
     */
    @Transactional
    public void restore(Integer id) {
        deletedAccountMapper.restore(id);
    }

    /**
     * 選択された複数の論理削除済みアカウント情報を一括で復元する。
     * 
     * @param ids        復元対象となるアカウントIDのリスト
     */
    @Transactional
    public void restoreBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {        
        	deletedAccountMapper.restoreBulk(ids);
        }
    }

    /**
     * 論理削除済みアカウントを一件物理削除する。
     * 
     * @param id 削除対象のアカウントID
     */
    @Transactional
    public void physicalDelete(Integer id) {
        deletedAccountMapper.physicalDelete(id);
    }

    /**
     * 選択された複数の論理削除済みアカウント情報を一括で物理削除する。
	 *
	 * @param ids 削除対象となるアカウントIDのリスト
     */
    @Transactional
    public void physicalDeleteBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedAccountMapper.physicalDeleteBulk(ids);
        }
    }

    /** 
     * 単一アカウントに紐づく社員情報が存在するか判定する。
     * 
     * @param id 判定対象のアカウントID
     * @return 存在していればtrue
     */
    public boolean existEmployeesByAccountId(Integer id) {
        return deletedAccountMapper.existEmployeesByAccountId(id) > 0;
    }

    /**
     * 複数アカウントの中に、紐づく社員情報が存在するものが含まれているか判定する。
     * 
     * @param id 判定対象のアカウントID
     * @return 含まれていればtrue
     */
    public boolean existEmployeesByAccountIds(List<Integer> ids) {
        return deletedAccountMapper.existEmployeesByAccountIds(ids) > 0;
    }
    
    /**
	 * ログインIDがすでに登録されているか（重複しているか）を判定する。
	 *
	 * @param execludeAccountId チェックするログインID
	 * @return 重複していればtrue
	 */
	public boolean isLoginIdDuplicate(Integer excludeAccountId) {
		int count = deletedAccountMapper.countByLoginId(excludeAccountId);
		return count > 0;
	}
	
	/**
	 * 複数のログインIDがすでに一つでも登録されているか（重複しているか）を判定する。
	 *
	 * @param execludeAccountIds チェックするログインIDのリスト
	 * @return 一つでも重複していればtrue
	 */
	public boolean isLoginIdDuplicate(List<Integer> excludeAccountIds) {
		int count = deletedAccountMapper.countByLoginIds(excludeAccountIds);
		return count > 0;
	}
	/**
	* 復元した結果、登録上限（500件）を超えるか判定する
	* @param restoreCount 復元しようとしている件数（単一なら1、一括ならリストのサイズ）
	* @return 500件を超える場合はtrue
	*/
	public boolean isAccountLimitReachedAfterRestore(int restoreCount) {
		// 1. 現在有効な（削除されていない）企業数を取得する
		// ※ 既存のCompanyService等から取得するか、独自に count を取得してください
		int currentActiveCount = deletedAccountMapper.countActiveAccounts();
		System.out.println(currentActiveCount);
		// 2. 現在の有効数 + これから復元する件数 が 500 を超えるかチェック
		return (currentActiveCount + restoreCount) > 500;
	}

}
