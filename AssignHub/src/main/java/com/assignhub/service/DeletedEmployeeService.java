package com.assignhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.assignhub.entity.Employee;
import com.assignhub.mapper.DeletedEmployeeMapper;

public class DeletedEmployeeService {
	@Autowired
    private DeletedEmployeeMapper deletedEmployeeMapper;

    /**
     * 論理削除済みのアカウント情報を全件取得する（一覧表示・検索用）。
     */
    public List<Employee> findAll(String keyword, String sort, String order, Integer permission) {
        return deletedEmployeeMapper.findAll(keyword, sort, order, permission);
    }

    /**
     * 選択されたアカウントの情報を取得する（CSVエクスポート用）。
     *
     * @param ids 画面のチェックボックスで選択されたアカウントIDのリスト
     * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    public List<Employee> findByIds(List<Integer> ids) {
        if (ids != null && ids.isEmpty()) {
        	return deletedEmployeeMapper.findByIds(ids);
        }
        return null;
    }

    /**
     * 単一データをゴミ箱から復元する。
     */
    @Transactional
    public void restore(Integer id) {
    	
        deletedEmployeeMapper.restore(id);
    }

    /**
     * 選択された複数のデータを一括でゴミ箱から復元する。
     * * @throws IllegalArgumentException 対象が選択されていない場合（画面へのエラーメッセージ用）
     */
    @Transactional
    public void restoreBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {        
        	deletedEmployeeMapper.restoreBulk(ids);
        }
    }

    /**
     * 単一データをデータベースから完全に削除する。
     */
    @Transactional
    public void physicalDelete(Integer id) {
        deletedEmployeeMapper.physicalDelete(id);
    }

    /**
     * 選択された複数のデータをデータベースから完全に一括削除する。
     */
    @Transactional
    public void physicalDeleteBulk(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            deletedEmployeeMapper.physicalDeleteBulk(ids);
        }
    }

    // =======================================================
    // Controllerからのチェック用メソッド（booleanを返す）
    // =======================================================

    /** 単一社員情報に紐づくアカウントが存在するか判定 生存*/
    public boolean countAccountsByEmpolyeeId(Integer id) {
        return deletedEmployeeMapper.countAccountsByEmpolyeeId(id) > 0;
    }

    /** 複数社員情報の中に、紐づくアカウントが存在するものが含まれているか判定 生存*/
    public boolean countAccountsByEmpolyeeIds(List<Integer> ids) {
        return deletedEmployeeMapper.countAccountsByEmpolyeeIds(ids) > 0;
    }

    /** 単一社員情報に紐づく企業情報（パートナー所属）が存在するか判定 生存*/
    public boolean countCompaniesByEmpolyeeId(Integer id) {
        return deletedEmployeeMapper.countCompaniesByEmpolyeeId(id) > 0;
    }

    /** 複数社員情報の中に、企業情報（パートナー所属）が存在するものが含まれているか判定 生存*/
    public boolean countCompaniesByEmpolyeeIds(List<Integer> ids) {
        return deletedEmployeeMapper.countCompaniesByEmpolyeeIds(ids) > 0;
    }
    
    
    /** 単一社員情報に紐づくアサイン情報が存在するか判定 不在*/
    public boolean countAssignmentsByEmpolyeeId(Integer id) {
        return deletedEmployeeMapper.countAssignmentsByEmpolyeeId(id) > 0;
    }

    /** 複数社員情報の中に、アサイン情報が存在するものが含まれているか判定 不在*/
    public boolean countAssignmentsByEmpolyeeIds(List<Integer> ids) {
        return deletedEmployeeMapper.countAssignmentsByEmpolyeeIds(ids) > 0;
    }
    
}
